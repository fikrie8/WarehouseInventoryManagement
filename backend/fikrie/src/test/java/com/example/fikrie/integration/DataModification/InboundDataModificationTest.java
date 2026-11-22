package com.example.fikrie.integration.DataModification;

import com.example.fikrie.DataTransferObject.RequestRespondInbound;
import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Model.Inbound;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
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
public class InboundDataModificationTest {

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

    /*I need to delete in these order since inbound and outbound is connected with inventory.
    To be able to delete inventory table, I need to clear all the table related to it.
     */
    @BeforeEach
    void cleanUpDatabase() {
        userRepo.deleteAll();
        inboundRepo.deleteAll();
        outboundRepo.deleteAll();
        inventoryRepo.deleteAll();
    }

    @Test
    @DisplayName("End-to-End PUT Inbound: Should successfully update inbound records with real model relations")
    void updateInboundPayloadPersistsInDb() throws Exception {
        Users manager = new Users();
        manager.setUsername("JohnManager");
        manager.setEmail("manager@example.com");
        manager.setRole("MANAGER");
        userRepo.save(manager);
        String realToken = jwtService.generateToken("JohnManager");
        Inventory item = new Inventory();
        item.setName("Camera");
        item.setSku("PWD11007890AP01");
        item.setLocation("Store A");
        item.setCategory("Equipment");
        item.setSupplier("Samsung");
        item.setQuantity(100);
        Inventory savedInventory = inventoryRepo.save(item);
        Inbound existingInbound = new Inbound();
        existingInbound.setReference("REF-2026-001");
        existingInbound.setDateReceived(new Date());
        existingInbound.setQuantity(50);
        existingInbound.setLocation("Store A");
        existingInbound.setRemarks("Initial batch setup");
        existingInbound.setInventory(savedInventory);
        Inbound savedInboundEntity = inboundRepo.save(existingInbound);
        int targetInboundId = savedInboundEntity.getId();
        RequestRespondInbound requestPayload = new RequestRespondInbound();
        requestPayload.setReference("REF-2026-001");
        requestPayload.setProductSku("PWD11007890AP01");
        requestPayload.setQuantity(500);
        requestPayload.setLocation("Store B");
        requestPayload.setRemarks("Bulk shipment update");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(realToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestRespondInbound> requestEntity = new HttpEntity<>(requestPayload, headers);
        ResponseEntity<RequestRespondInbound> response = restTemplate.exchange(
                "/inbound/update/" + targetInboundId,
                HttpMethod.PUT,
                requestEntity,
                RequestRespondInbound.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        Inbound verifiedRecord = inboundRepo.findById((long) savedInboundEntity.getId())
                .orElseThrow(() -> new AssertionError("Inbound entity record was missing from database"));
        assertThat(verifiedRecord.getQuantity()).isEqualTo(500);
        assertThat(verifiedRecord.getLocation()).isEqualTo("Store B");
        assertThat(verifiedRecord.getInventory().getSku()).isEqualTo("PWD11007890AP01");
        Inventory verifiedInventory = inventoryRepo.findBySku(verifiedRecord.getInventory().getSku())
                .orElseThrow(() -> new AssertionError("Inventory record was missing from database"));
        assertThat(verifiedInventory.getQuantity()).isEqualTo(550);
    }
}
