package com.example.fikrie.integration.CyberSecurityAttack;

import com.example.fikrie.DataTransferObject.RequestRespondInventory;
import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Model.Outbound;
import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.InboundRepo;
import com.example.fikrie.Repository.InventoryRepo;
import com.example.fikrie.Repository.OutboundRepo;
import com.example.fikrie.Repository.UserRepo;
import com.example.fikrie.Service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.jwt.secret-key=dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLXRlc3RpbmctYXV0aGVudGljYXRpb24taW50ZWdyYXRpb24=",
                "ADMIN_EMAIL=admin@example.com",
                "ADMIN_PASSWORD=adminPassword12345"
        }
)
@AutoConfigureTestRestTemplate
public class SecurityHardeningTest {

    @Autowired
    private InboundRepo inboundRepo;

    @Autowired
    private OutboundRepo outboundRepo;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private InventoryRepo inventoryRepo;

    private String managerToken;

    @BeforeEach
    void setUp() {
        inboundRepo.deleteAll();
        outboundRepo.deleteAll();
        inventoryRepo.deleteAll();
        userRepo.deleteAll();

        Users manager = new Users();
        manager.setUsername("JohnManager");
        manager.setEmail("manager@example.com");
        manager.setRole("MANAGER");
        userRepo.save(manager);

        this.managerToken = jwtService.generateToken("JohnManager");
    }

    @Test
    @DisplayName("SQL Injection Defense: Login should treat SQL syntax as literal text and reject with 401")
    void loginWithSqlInjectionRejected() {
        RequestRespondUser maliciousLogin = new RequestRespondUser();
        maliciousLogin.setUsername("admin' OR '1'='1");
        maliciousLogin.setPassword("anything");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(
                "/login",
                HttpMethod.POST,
                new HttpEntity<>(maliciousLogin, headers),
                String.class
        );

        // Parameterized JPA queries treat the input as a literal username, failing authentication cleanly
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("JWT Integrity Defense: Tampered token signature must be blocked with 401/403")
    void tamperedJwtSignatureBlocked() {
        // Alter the signature portion (last 5 characters) of a valid token
        String tamperedToken = managerToken.substring(0, managerToken.length() - 5) + "XXXXX";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tamperedToken);
        ResponseEntity<String> response = restTemplate.exchange(
                "/user/get-user-info/JohnManager",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Information Leak Defense: Error responses must not expose internal SQL or Java stack traces")
    void errorResponseDoesNotLeakSensitiveInternals() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(managerToken);
        // Request a non-existent SKU to trigger a 404/400 error response
        ResponseEntity<String> response = restTemplate.exchange(
                "/inventory/get-inventory/NON-EXISTENT-SKU",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
        if (response.getBody() != null) {
            assertThat(response.getBody()).doesNotContain("java.sql.", "org.hibernate.", "Exception:");
        }
    }

    @Test
    @DisplayName("Input Validation Defense: Reject overly long or malformed SKU strings cleanly")
    void createInventoryWithOversizedPayload() {
        RequestRespondInventory oversizedPayload = new RequestRespondInventory();
        oversizedPayload.setName("Camera");
        // 500-character string designed to exceed standard VARCHAR(255) column limits
        oversizedPayload.setSku("A".repeat(500));
        oversizedPayload.setLocation("Store A");
        oversizedPayload.setCategory("Equipment");
        oversizedPayload.setSupplier("Samsung");
        oversizedPayload.setQuantity(10);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(managerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(
                "/inventory/register-inventory/single-upload",
                HttpMethod.POST,
                new HttpEntity<>(oversizedPayload, headers),
                String.class
        );

        // GlobalExceptionHandler catches DataIntegrityViolationException and returns 400 or 409 instead of crashing
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.CONFLICT);
    }
}
