package com.example.fikrie.integration.DataModification;

import com.example.fikrie.DataTransferObject.RequestRespondUser;
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
                "app.jwt.secret-key=dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLXRlc3RpbmctYXV0aGVudGljYXRpb24taW50ZWdyYXRpb24=",
                "ADMIN_EMAIL=admin@example.com",
                "ADMIN_PASSWORD=adminPassword12345"
        }
)
@AutoConfigureTestRestTemplate
public class UserDataModificationTest {

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
    @DisplayName("End-to-End PUT User: Should successfully update user records inside the real database table")
    void updateUserPayloadPersistsInDb() throws Exception {
        Users existingUser = new Users();
        existingUser.setUsername("John2");
        existingUser.setEmail("JohnOld@email.com");
        existingUser.setRole("USER");
        existingUser.setDepartment("USER");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setPassword("PasswordForJohn");
        Users savedUser = userRepo.save(existingUser);
        String realToken = jwtService.generateToken("John2");
        RequestRespondUser requestPayload = new RequestRespondUser();
        requestPayload.setUsername("John2");
        requestPayload.setEmail("John2@email.com");
        requestPayload.setRole("USER");
        requestPayload.setDepartment("HR");
        Users innerUserUpdateData = new Users();
        innerUserUpdateData.setUsername("John2");
        innerUserUpdateData.setEmail("John2@email.com");
        innerUserUpdateData.setRole("USER");
        innerUserUpdateData.setDepartment("HR");
        innerUserUpdateData.setFirstName("John");
        innerUserUpdateData.setLastName("Doe");
        requestPayload.setUser(innerUserUpdateData);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(realToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestRespondUser> requestEntity = new HttpEntity<>(requestPayload, headers);
        ResponseEntity<RequestRespondUser> response = restTemplate.exchange(
                "/user/update-user",
                HttpMethod.PUT,
                requestEntity,
                RequestRespondUser.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("John2");
        assertThat(response.getBody().getUser()).isNotNull();
        assertThat(response.getBody().getUser().getEmail()).isEqualTo("John2@email.com");
        assertThat(response.getBody().getUser().getDepartment()).isEqualTo("HR");
        Users verificationRecord = userRepo.findById(savedUser.getId())
                .orElseThrow(() -> new AssertionError("User entity record was unexpectedly missing from DB"));
        assertThat(verificationRecord.getEmail()).isEqualTo("John2@email.com");
        assertThat(verificationRecord.getDepartment()).isEqualTo("HR");
    }
}
