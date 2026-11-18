package com.example.fikrie.integration.AuthorizationAndSecurity;

import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.UserRepo;
import com.example.fikrie.Service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "jwt.secret-key=dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLXRlc3RpbmctYXV0aGVudGljYXRpb24taW50ZWdyYXRpb24=",
                "ADMIN_EMAIL=admin@email.com",
                "ADMIN_PASSWORD=adminPassword12345"
        }
)
@AutoConfigureTestRestTemplate
public class AuthorizationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void cleanUpDatabase() {
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("End-to-End Admin Route: Should allow access when valid ADMIN token is supplied")
    void adminRoleToAdminRouteSucceeds() throws Exception {
        Users adminUser = new Users();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole("ADMIN");
        userRepo.save(adminUser);
        String realAdminToken = jwtService.generateToken("admin");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(realAdminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/get-user-info/all",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("End-to-End Admin Route: Should return 403 Forbidden when a non-admin token attempts entry")
    void userRoleToAdminRouteForbidden() throws Exception {
        Users regularUser = new Users();
        regularUser.setUsername("regular_john");
        regularUser.setEmail("john@example.com");
        regularUser.setRole("USER");
        userRepo.save(regularUser);
        String regularUserToken = jwtService.generateToken("regular_john");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(regularUserToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/get-user-info/all",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("End-to-End Method Security: Should allow manager trying to view a different user's information profile")
    void managerRoleToDifferentUserRouteSucceed() throws Exception {
        Users regularUser = new Users();
        regularUser.setUsername("JohnManager");
        regularUser.setEmail("JohnManager@example.com");
        regularUser.setRole("MANAGER");
        userRepo.save(regularUser);
        Users targetUser = new Users();
        targetUser.setUsername("Jane");
        targetUser.setEmail("Jane@example.com");
        targetUser.setRole("USER");
        userRepo.save(targetUser);
        String johnsToken = jwtService.generateToken("JohnManager");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(johnsToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/user/get-user-info/Jane",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("End-to-End Method Security: Should block user trying to view a different user's information profile")
    void differentUsernameToDifferentUserRouteForbidden() throws Exception {
        Users regularUser = new Users();
        regularUser.setUsername("John");
        regularUser.setEmail("john@example.com");
        regularUser.setRole("USER");
        userRepo.save(regularUser);
        String johnsToken = jwtService.generateToken("John");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(johnsToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/user/get-user-info/Jane",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
