package com.example.fikrie.Service;

import com.example.fikrie.DataTransferObject.RequestRespondInbound;
import com.example.fikrie.Exception.ResourceNotFoundException;
import com.example.fikrie.Model.Inbound;
import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Repository.InboundRepo;
import com.example.fikrie.Repository.InventoryRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class InboundService {

    private final InboundRepo inboundRepo;
    private final InventoryRepo inventoryRepo;

    public RequestRespondInbound getInboundByReference(String requestInboundReference) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        Inbound inbound = inboundRepo.findByReference(requestInboundReference)
                    .orElseThrow(() -> new ResourceNotFoundException("getInboundByReference failed: Inbound not found by Reference: " + requestInboundReference));
        requestRespondInbound.setReference(inbound.getReference());
        requestRespondInbound.setDateReceived(inbound.getDateReceived());
        requestRespondInbound.setProductSku(inbound.getInventory().getSku());
        requestRespondInbound.setQuantity(inbound.getQuantity());
        requestRespondInbound.setLocation(inbound.getLocation());
        requestRespondInbound.setRemarks(inbound.getRemarks());
        requestRespondInbound.setStatusCode(200);
        requestRespondInbound.setMessage("Inbound with reference: " + requestInboundReference + " found successfully");

        return requestRespondInbound;
    }

    public RequestRespondInbound getInboundBySku(String requestInboundSku) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        List<Inbound> listOfInbounds = inboundRepo.findByInventorySku(requestInboundSku);
        if(listOfInbounds.isEmpty()) {
            throw new ResourceNotFoundException("getInboundBySku failed: Inbound not found by Sku: " + requestInboundSku);
        }
        requestRespondInbound.setListOfInbound(listOfInbounds);
        requestRespondInbound.setStatusCode(200);
        requestRespondInbound.setMessage("Inbound with productSku: " + requestInboundSku + " found successfully");

        return requestRespondInbound;
    }

    public RequestRespondInbound getAllInbound() {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        List<Inbound> listOfInbounds = inboundRepo.findAll();
        if(listOfInbounds.isEmpty()) {
            throw new ResourceNotFoundException("getAllInbound failed: No inbounds record is found in the system");
        }
        requestRespondInbound.setListOfInbound(listOfInbounds);
        requestRespondInbound.setStatusCode(200);
        requestRespondInbound.setMessage("All inbound found successfully");

        return requestRespondInbound;
    }

    @Transactional
    public RequestRespondInbound registerInbound(RequestRespondInbound inboundRegistrationRequest) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        Inventory inventory = inventoryRepo.findBySku(inboundRegistrationRequest.getProductSku())
                .orElseThrow(() -> new ResourceNotFoundException("registerInbound failed: Inbound not found by Sku: " + inboundRegistrationRequest.getProductSku()));
        if(inventory.getLocation().equalsIgnoreCase(inboundRegistrationRequest.getLocation())) {
            Inbound inbound = new Inbound();
            if(Objects.nonNull(inboundRegistrationRequest.getReference()) && !inboundRegistrationRequest.getReference().isEmpty()) {
                inbound.setReference(inboundRegistrationRequest.getReference());
            } else {
                long nextId = inboundRepo.count() + 1;
                String referenceTicket = String.format("INBOUND%04d",nextId);
                inbound.setReference(referenceTicket);
            }
            if(Objects.nonNull(inboundRegistrationRequest.getDateReceived())) {
                inbound.setDateReceived(inboundRegistrationRequest.getDateReceived());
            } else {
                inbound.setDateReceived(new Date());
            }
            inbound.setInventory(inventory);
            inbound.setQuantity(inboundRegistrationRequest.getQuantity());
            inbound.setLocation(inboundRegistrationRequest.getLocation());
            inbound.setRemarks(inboundRegistrationRequest.getRemarks());
            Inbound inboundRegistered = inboundRepo.save(inbound);

            /*
                For existing database records that you looked up using a repository, simply use your .set() methods inside a @Transactional block.
                Hibernate handles the persistence lifecycle completely.
                It's called Automatic Dirty Checking when using @Transactional
            */
            inventory.setQuantity(inventory.getQuantity() + inboundRegistered.getQuantity());
            requestRespondInbound.setStatusCode(200);
            requestRespondInbound.setListOfInbound(List.of(inbound));
            requestRespondInbound.setMessage("Inbound has been registered successfully");
        }

        return requestRespondInbound;
    }

    public RequestRespondInbound updateInbound(Long inboundId, RequestRespondInbound requestUpdateInbound) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        if(requestUpdateInbound.getQuantity() < 0){
            throw new DataIntegrityViolationException("updateInbound failed: Quantity must be more than 0");
        }
        Inbound oldInbound = inboundRepo.findById(inboundId).
                orElseThrow(() -> new ResourceNotFoundException("updateInbound failed: Inbound not found by Id : " + inboundId));
        Inventory inventory = inventoryRepo.findBySku(requestUpdateInbound.getProductSku())
                .orElseThrow(() -> new ResourceNotFoundException("updateInbound failed: Inventory not found by Sku: " + requestUpdateInbound.getProductSku()));
        inventory.setQuantity(inventory.getQuantity() - oldInbound.getQuantity());
        Inbound inbound = getInboundFields(requestUpdateInbound, oldInbound);
        Inbound updateInbound = inboundRepo.save(inbound);
        inventory.setQuantity(inventory.getQuantity() + inbound.getQuantity());
        requestRespondInbound.setStatusCode(200);
        requestRespondInbound.setMessage("Inbound updated successfully");

        return requestRespondInbound;
    }

    public RequestRespondInbound deleteInbound(Long requestInboundById) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        Inbound inbound = inboundRepo.findById(requestInboundById)
                .orElseThrow(() -> new ResourceNotFoundException("deleteInbound failed: Inbound not be found by Id: " + requestInboundById));
        Inventory inventory = inbound.getInventory();
        int inboundQuantity = inbound.getQuantity();
        inboundRepo.deleteById(requestInboundById);
        inventory.setQuantity(inventory.getQuantity() - inboundQuantity);
        requestRespondInbound.setStatusCode(200);
        requestRespondInbound.setMessage("Inbound successfully deleted");

        return requestRespondInbound;
    }

    private Inbound getInboundFields(RequestRespondInbound requestUpdateInbound, Inbound inbound) {
        if(Objects.nonNull(requestUpdateInbound.getReference()) && !requestUpdateInbound.getReference().isEmpty()) {
            inbound.setReference(requestUpdateInbound.getReference());
        }
        if(Objects.nonNull(requestUpdateInbound.getDateReceived())) {
            inbound.setDateReceived(requestUpdateInbound.getDateReceived());
        }
        if(Objects.nonNull(requestUpdateInbound.getProductSku())) {
            Inventory inventory = inventoryRepo.findBySku(requestUpdateInbound.getProductSku())
                    .orElseThrow(() -> new ResourceNotFoundException("getInbound failed: Inbound not found by Sku: " + requestUpdateInbound.getProductSku()));
            inbound.setInventory(inventory);
        }
        if(Objects.nonNull(requestUpdateInbound.getQuantity())) {
            inbound.setQuantity(requestUpdateInbound.getQuantity());
        }
        if(Objects.nonNull(requestUpdateInbound.getLocation()) && !requestUpdateInbound.getLocation().isEmpty()) {
            inbound.setLocation(requestUpdateInbound.getLocation());
        }
        if(Objects.nonNull(requestUpdateInbound.getRemarks()) && !requestUpdateInbound.getRemarks().isEmpty()) {
            inbound.setRemarks(requestUpdateInbound.getRemarks());
        }
        return inbound;
    }
}
