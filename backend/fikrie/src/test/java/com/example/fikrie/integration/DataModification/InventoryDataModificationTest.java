package com.example.fikrie.integration.DataModification;

import com.example.fikrie.DataTransferObject.RequestRespondInventory;
import com.example.fikrie.Model.Inventory;
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
public class InventoryDataModificationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private InventoryRepo inventoryRepo;

    @Autowired
    private InboundRepo inboundRepo;

    @Autowired
    private OutboundRepo outboundRepo;

    /* Need to delete inbound & outbound before able to delete inventory*/
    @BeforeEach
    void cleanUpDatabase() {

        userRepo.deleteAll();
        inboundRepo.deleteAll();
        outboundRepo.deleteAll();
        inventoryRepo.deleteAll();
    }

    @Test
    @DisplayName("End-to-End PUT Inventory: Should successfully update inventory records inside the real database table")
    void updateInventoryPayloadPersistsInDb() throws Exception {
        Users existingUser = new Users();
        existingUser.setUsername("JohnManager");
        existingUser.setEmail("JohnManager@email.com");
        existingUser.setRole("MANAGER");
        existingUser.setDepartment("MANAGEMENT");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setPassword("PasswordForJohn");
        Users savedUser = userRepo.save(existingUser);
        Inventory existingInventory = new Inventory();
        existingInventory.setName("Camera");
        existingInventory.setSku("PWD11007890APO1");
        existingInventory.setLocation("Store A");
        existingInventory.setCategory("Equipment");
        existingInventory.setSupplier("Samsung");
        existingInventory.setQuantity(100);
        Inventory savedInventory = inventoryRepo.save(existingInventory);
        String realToken = jwtService.generateToken("JohnManager");
        RequestRespondInventory requestPayload = new RequestRespondInventory();
        requestPayload.setName("Camera");
        requestPayload.setSku("PWD11007890APO1");
        requestPayload.setLocation("Store A");
        requestPayload.setCategory("Equipment");
        requestPayload.setSupplier("Samsung");
        requestPayload.setQuantity(100);
        Inventory innerInventoryUpdateData = new Inventory();
        innerInventoryUpdateData.setName("Digital Camera");
        innerInventoryUpdateData.setSku("PWD11007890AP01");
        innerInventoryUpdateData.setLocation("Store Z");
        innerInventoryUpdateData.setCategory("Digital Equipment");
        innerInventoryUpdateData.setSupplier("Samsung");
        innerInventoryUpdateData.setQuantity(100);
        requestPayload.setInventory(innerInventoryUpdateData);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(realToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestRespondInventory> requestEntity = new HttpEntity<>(requestPayload, headers);
        ResponseEntity<RequestRespondInventory> response = restTemplate.exchange(
                "/inventory/update-inventory",
                HttpMethod.PUT,
                requestEntity,
                RequestRespondInventory.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getInventory()).isNotNull();
        assertThat(response.getBody().getInventory().getName()).isEqualTo("Digital Camera");
        assertThat(response.getBody().getInventory().getSku()).isEqualTo("PWD11007890AP01");
        assertThat(response.getBody().getInventory().getLocation()).isEqualTo("Store Z");
        assertThat(response.getBody().getInventory().getCategory()).isEqualTo("Digital Equipment");
        assertThat(response.getBody().getInventory().getQuantity()).isEqualTo(100);
        Inventory verificationRecord = inventoryRepo.findById(savedInventory.getId())
                .orElseThrow(() -> new AssertionError("Inventory entity record was unexpectedly missing from DB"));
        assertThat(verificationRecord.getName()).isEqualTo("Digital Camera");
        assertThat(verificationRecord.getSku()).isEqualTo("PWD11007890AP01");
        assertThat(verificationRecord.getLocation()).isEqualTo("Store Z");
        assertThat(verificationRecord.getCategory()).isEqualTo("Digital Equipment");
        assertThat(verificationRecord.getQuantity()).isEqualTo(100);
    }
}
