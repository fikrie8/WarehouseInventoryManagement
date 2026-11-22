package com.example.fikrie.integration.DataModification;

import com.example.fikrie.DataTransferObject.RequestRespondInbound;
import com.example.fikrie.DataTransferObject.RequestRespondOutbound;
import com.example.fikrie.Model.Inbound;
import com.example.fikrie.Model.Inventory;
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

import java.util.Date;

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
public class OutboundDataModificationTest {

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

    private String managerToken;
    private Inventory savedInventory;

    @BeforeEach
    void cleanUpDatabase() {
        userRepo.deleteAll();
        inboundRepo.deleteAll();
        outboundRepo.deleteAll();
        inventoryRepo.deleteAll();

        // 2. Seed Manager & generate valid JWT token for all tests
        Users manager = new Users();
        manager.setUsername("JohnManager");
        manager.setEmail("manager@example.com");
        manager.setRole("MANAGER");
        userRepo.save(manager);
        this.managerToken = jwtService.generateToken("JohnManager");

        // 3. Seed baseline Inventory item with 1000 stock
        Inventory item = new Inventory();
        item.setName("Camera");
        item.setSku("PWD11007890AP01");
        item.setLocation("Store A");
        item.setCategory("Equipment");
        item.setSupplier("Samsung");
        item.setQuantity(1000);
        this.savedInventory = inventoryRepo.save(item);
    }

    @Test
    @DisplayName("End-to-End PUT Outbound: Should successfully update outbound records with real model relations")
    void updateOutboundPayloadPersistsInDb() throws Exception {
        Outbound existingOutbound = new Outbound();
        existingOutbound.setReference("REF-2025-001");
        existingOutbound.setDateShipped(new Date());
        existingOutbound.setQuantity(50);
        existingOutbound.setDestination("Store A");
        existingOutbound.setRemarks("Initial batch setup");
        existingOutbound.setInventory(savedInventory);
        Outbound savedOutboundEntity = outboundRepo.save(existingOutbound);
        int targetOutboundId = savedOutboundEntity.getId();
        RequestRespondOutbound requestPayload = new RequestRespondOutbound();
        requestPayload.setReference("REF-2026-001");
        requestPayload.setProductSku("PWD11007890AP01");
        requestPayload.setQuantity(500);
        requestPayload.setDestination("Store B");
        requestPayload.setRemarks("Bulk shipment update");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(managerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestRespondOutbound> requestEntity = new HttpEntity<>(requestPayload, headers);
        ResponseEntity<RequestRespondOutbound> response = restTemplate.exchange(
                "/outbound/update/" + targetOutboundId,
                HttpMethod.PUT,
                requestEntity,
                RequestRespondOutbound.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        Outbound verifiedRecord = outboundRepo.findById((long) savedOutboundEntity.getId())
                .orElseThrow(() -> new AssertionError("Outbound entity record was missing from database"));
        assertThat(verifiedRecord.getReference()).isEqualTo("REF-2026-001");
        assertThat(verifiedRecord.getQuantity()).isEqualTo(500);
        assertThat(verifiedRecord.getDestination()).isEqualTo("Store B");
        assertThat(verifiedRecord.getInventory().getSku()).isEqualTo("PWD11007890AP01");
        Inventory verifiedInventory = inventoryRepo.findBySku(verifiedRecord.getInventory().getSku())
                .orElseThrow(() -> new AssertionError("Inventory record was missing from database"));
        assertThat(verifiedInventory.getQuantity()).isEqualTo(550);
    }

    @Test
    @DisplayName("POST Outbound (Negative Case): Should return 400 Bad Request when quantity exceeds available stock")
    void registerOutboundExceedingStockReturnsBadRequest() {
        RequestRespondOutbound requestPayload = new RequestRespondOutbound();
        requestPayload.setProductSku("PWD11007890AP01");
        requestPayload.setQuantity(5000);
        requestPayload.setDestination("Store B");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(managerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(
                "/outbound/register",
                HttpMethod.POST,
                new HttpEntity<>(requestPayload, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(outboundRepo.count()).isZero();
        Inventory untouchedInventory = inventoryRepo.findBySku("PWD11007890AP01")
                .orElseThrow(() -> new AssertionError("Inventory record missing from DB"));
        assertThat(untouchedInventory.getQuantity()).isEqualTo(1000);
    }
}
