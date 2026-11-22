package com.example.fikrie.integration.AuthorizationAndSecurity;

import com.example.fikrie.DataTransferObject.RequestRespondUser;
import com.example.fikrie.Repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
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
                "ADMIN_EMAIL=admin@email.com",
                "ADMIN_PASSWORD=adminPassword12345"
        }
)
@AutoConfigureTestRestTemplate
@Slf4j
public class RegisterLoginTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void cleanUpDatabase() {
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("End-to-End Register: Should successfully save a new user to the database and return 200 OK")
    void registrationSucceeds() {
        RequestRespondUser registerPayload = new RequestRespondUser();
        registerPayload.setUsername("John");
        registerPayload.setEmail("John@email.com");
        registerPayload.setPassword("JohnSecurePassword123");
        registerPayload.setRole("USER");
        registerPayload.setDepartment("USER");
        registerPayload.setFirstName("John");
        registerPayload.setLastName("Doe");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestRespondUser> requestEntity = new HttpEntity<>(registerPayload, headers);
        ResponseEntity<RequestRespondUser> response = restTemplate.exchange(
                "/register",
                HttpMethod.POST,
                requestEntity,
                RequestRespondUser.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        boolean existsInDb = userRepo.findAll().stream()
                .anyMatch(user -> user.getUsername().equals("John"));
        assertThat(existsInDb).isTrue();
    }

    @Test
    @DisplayName("End-to-End Login: Should successfully authenticate an existing user and return a token")
    void loginSucceedAndReturnToken() {
        RequestRespondUser setupPayload = new RequestRespondUser();
        setupPayload.setUsername("Jane");
        setupPayload.setEmail("Jane@email.com");
        setupPayload.setPassword("JaneSecretPassword123");
        setupPayload.setRole("MANAGER");
        HttpHeaders registerHeaders = new HttpHeaders();
        registerHeaders.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(
                "/register",
                HttpMethod.POST,
                new HttpEntity<>(setupPayload, registerHeaders),
                RequestRespondUser.class
        );
        RequestRespondUser loginPayload = new RequestRespondUser();
        loginPayload.setUsername("Jane");
        loginPayload.setPassword("JaneSecretPassword123");
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestRespondUser> requestEntity = new HttpEntity<>(loginPayload, loginHeaders);
        ResponseEntity<RequestRespondUser> response = restTemplate.exchange(
                "/login",
                HttpMethod.POST,
                requestEntity,
                RequestRespondUser.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
