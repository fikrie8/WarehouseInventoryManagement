package com.example.fikrie.Service;

import com.example.fikrie.DataTransferObject.RequestRespondInbound;
import com.example.fikrie.Model.Inbound;
import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Repository.InboundRepo;
import com.example.fikrie.Repository.InventoryRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InboundService {

    private final InboundRepo inboundRepo;
    private final InventoryRepo inventoryRepo;

    public RequestRespondInbound getInboundByReference(String requestInboundReference) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        try {
            Inbound inbound = inboundRepo.findByReference(requestInboundReference);
            if(Objects.nonNull(inbound)) {
                requestRespondInbound.setReference(inbound.getReference());
                requestRespondInbound.setDateReceived(inbound.getDateReceived());
                requestRespondInbound.setProductSku(inbound.getInventory().getSku());
                requestRespondInbound.setQuantity(inbound.getQuantity());
                requestRespondInbound.setLocation(inbound.getLocation());
                requestRespondInbound.setRemarks(inbound.getRemarks());
                requestRespondInbound.setStatusCode(200);
                requestRespondInbound.setMessage("Inbound with reference : " + requestInboundReference + " found successfully");
            } else {
                requestRespondInbound.setStatusCode(404);
                requestRespondInbound.setMessage("Inbound not found ");
            }
        } catch (Exception e) {
            requestRespondInbound.setStatusCode(500);
            requestRespondInbound.setMessage("Error occurred when getting inbound by reference : " + e.getMessage());
        }
        return requestRespondInbound;
    }

    public RequestRespondInbound getInboundBySku(String requestInboundSku) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        try {
            List<Inbound> listOfInbounds = inboundRepo.findByInventorySku(requestInboundSku);
            if(listOfInbounds.size() > 0) {
                requestRespondInbound.setListOfInbound(listOfInbounds);
                requestRespondInbound.setStatusCode(200);
                requestRespondInbound.setMessage("Inbound with product_sku : " + requestInboundSku + " found successfully");
            } else {
                requestRespondInbound.setStatusCode(404);
                requestRespondInbound.setMessage("Inbound not found ");
            }
        } catch (Exception e) {
            requestRespondInbound.setStatusCode(500);
            requestRespondInbound.setMessage("Error occurred when getting inbound by sku : " + e.getMessage());
        }
        return requestRespondInbound;
    }

    public RequestRespondInbound getAllInbound() {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        try {
            List<Inbound> listOfInbounds = inboundRepo.findAll();
            if(!listOfInbounds.isEmpty()) {
                requestRespondInbound.setListOfInbound(listOfInbounds);
                requestRespondInbound.setStatusCode(200);
                requestRespondInbound.setMessage("All inbound found successfully");
            } else {
                requestRespondInbound.setStatusCode(404);
                requestRespondInbound.setMessage("Inbound not found ");
            }
        } catch (Exception e) {
            requestRespondInbound.setStatusCode(500);
            requestRespondInbound.setMessage("Error occurred when getting all inbound : " + e.getMessage());
        }
        return requestRespondInbound;
    }

    @Transactional
    public RequestRespondInbound registerInbound(RequestRespondInbound inboundRegistrationRequest) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        try {
            Optional<Inventory> optionalInventory = inventoryRepo.findBySku(inboundRegistrationRequest.getProductSku());
            if(optionalInventory.isPresent() && (optionalInventory.get().getLocation().equalsIgnoreCase(inboundRegistrationRequest.getLocation()))) {
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
                inbound.setInventory(optionalInventory.get());
                inbound.setQuantity(inboundRegistrationRequest.getQuantity());
                inbound.setLocation(inboundRegistrationRequest.getLocation());
                inbound.setRemarks(inboundRegistrationRequest.getRemarks());
                Inbound inboundRegistered = inboundRepo.save(inbound);
                if(inboundRegistered.getId() > 0) {
                    Inventory updatedInventory = new Inventory();
                    updatedInventory.setId(optionalInventory.get().getId());
                    updatedInventory.setSku(optionalInventory.get().getSku());
                    updatedInventory.setCategory(optionalInventory.get().getCategory());
                    updatedInventory.setName(optionalInventory.get().getName());
                    updatedInventory.setLocation(optionalInventory.get().getLocation());
                    updatedInventory.setQuantity(optionalInventory.get().getQuantity() + inboundRegistered.getQuantity());
                    updatedInventory.setSupplier(optionalInventory.get().getSupplier());
                    Inventory inventoryRegistered = inventoryRepo.save(updatedInventory);
                    if(inventoryRegistered.getId() > 0) {
                        requestRespondInbound.setStatusCode(200);
                        requestRespondInbound.setMessage("Inbound has been registered successfully");
                    } else {
                        requestRespondInbound.setStatusCode(400);
                        requestRespondInbound.setMessage("Inbound failed to register. Something wrong when calling the repository save");
                    }
                } else {
                    requestRespondInbound.setStatusCode(400);
                    requestRespondInbound.setMessage("Inbound failed to register. Maybe wrong SKU or Location");
                }
            }
        } catch (Exception e) {
            requestRespondInbound.setStatusCode(500);
            requestRespondInbound.setMessage(e.getMessage());
        }
        return requestRespondInbound;
    }

    public RequestRespondInbound updateInbound(Long inboundId, RequestRespondInbound requestUpdateInbound) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        try {
            Optional<Inbound> optionalInbound = inboundRepo.findById(inboundId);
            if(optionalInbound.isPresent()) {
                Inbound inbound = getInbound(requestUpdateInbound, optionalInbound);
                Inbound updateInbound = inboundRepo.save(inbound);
                if(updateInbound.getId() > 0) {
                    requestRespondInbound.setStatusCode(200);
                    requestRespondInbound.setMessage("Inbound updated successfully");
                }
            } else {
                requestRespondInbound.setStatusCode(404);
                requestRespondInbound.setMessage("Inbound not found for update");
            }
        } catch (Exception e) {
            requestRespondInbound.setStatusCode(500);
            requestRespondInbound.setMessage("Error occurred when updating inbound :" + e.getMessage());
        }
        return requestRespondInbound;
    }

    public RequestRespondInbound deleteInbound(Long requestInboundById) {
        RequestRespondInbound requestRespondInbound = new RequestRespondInbound();
        try {
            Optional<Inbound> inboundOptional = inboundRepo.findById(requestInboundById);
            if(inboundOptional.isPresent()) {
                inboundRepo.deleteById(requestInboundById);
                requestRespondInbound.setStatusCode(200);
                requestRespondInbound.setMessage("Inbound successfully deleted");
            } else {
                requestRespondInbound.setStatusCode(404);
                requestRespondInbound.setMessage("Inbound not found for deletion");
            }
        } catch (Exception e) {
            requestRespondInbound.setStatusCode(500);
            requestRespondInbound.setMessage("Error occurred while deleting inbound :" + e.getMessage());
        }
        return requestRespondInbound;
    }

    private Inbound getInbound(RequestRespondInbound requestUpdateInbound, Optional<Inbound> optionalInbound) {
        Inbound inbound = optionalInbound.get();
        if(Objects.nonNull(requestUpdateInbound.getReference()) && !requestUpdateInbound.getReference().isEmpty()) {
            inbound.setReference(requestUpdateInbound.getReference());
        }
        if(Objects.nonNull(requestUpdateInbound.getDateReceived())) {
            inbound.setDateReceived(requestUpdateInbound.getDateReceived());
        }
        if(Objects.nonNull(requestUpdateInbound.getProductSku())) {
            Optional<Inventory> optionalInventory = inventoryRepo.findBySku(requestUpdateInbound.getProductSku());
            if (optionalInventory.isPresent()) {
                Inventory inventory = optionalInventory.get();
                inbound.setInventory(inventory);
            }
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
