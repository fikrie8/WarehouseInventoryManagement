package com.example.fikrie.Service;

import com.example.fikrie.DataTransferObject.RequestRespondOutbound;
import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Model.Outbound;
import com.example.fikrie.Repository.InventoryRepo;
import com.example.fikrie.Repository.OutboundRepo;
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
public class OutboundService {

    private final OutboundRepo outboundRepo;
    private final InventoryRepo inventoryRepo;

    public RequestRespondOutbound getOutboundByReference(String requestOutboundReference) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        try {
            Outbound outbound = outboundRepo.findByReference(requestOutboundReference);
            if(Objects.nonNull(outbound)) {
                requestRespondOutbound.setReference(outbound.getReference());
                requestRespondOutbound.setDateShipped(outbound.getDateShipped());
                requestRespondOutbound.setProductSku(outbound.getInventory().getSku());
                requestRespondOutbound.setQuantity(outbound.getQuantity());
                requestRespondOutbound.setDestination(outbound.getDestination());
                requestRespondOutbound.setRemarks(outbound.getRemarks());
                requestRespondOutbound.setStatusCode(200);
                requestRespondOutbound.setMessage("Outbound with reference : " + requestOutboundReference + " found successfully");
            } else {
                requestRespondOutbound.setStatusCode(404);
                requestRespondOutbound.setMessage("Outbound not found ");
            }
        } catch (Exception e) {
            requestRespondOutbound.setStatusCode(500);
            requestRespondOutbound.setMessage("Error occurred when getting outbound by reference : " + e.getMessage());
        }
        return requestRespondOutbound;
    }

    public RequestRespondOutbound getOutboundBySku(String requestOutboundSku) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        try {
            List<Outbound> listOfOutbounds = outboundRepo.findByInventorySku(requestOutboundSku);
            if(listOfOutbounds.size() > 0) {
                requestRespondOutbound.setListOfOutbound(listOfOutbounds);
                requestRespondOutbound.setStatusCode(200);
                requestRespondOutbound.setMessage("Outbound with product_sku : " + requestOutboundSku + " found successfully");
            } else {
                requestRespondOutbound.setStatusCode(404);
                requestRespondOutbound.setMessage("Outbound not found ");
            }
        } catch (Exception e) {
            requestRespondOutbound.setStatusCode(500);
            requestRespondOutbound.setMessage("Error occurred when getting outbound by sku : " + e.getMessage());
        }
        return requestRespondOutbound;
    }

    public RequestRespondOutbound getAllOutbound() {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        try {
            List<Outbound> listOfOutbounds = outboundRepo.findAll();
            if(!listOfOutbounds.isEmpty()) {
                requestRespondOutbound.setListOfOutbound(listOfOutbounds);
                requestRespondOutbound.setStatusCode(200);
                requestRespondOutbound.setMessage("All outbound found successfully");
            } else {
                requestRespondOutbound.setStatusCode(404);
                requestRespondOutbound.setMessage("Outbound not found ");
            }
        } catch (Exception e) {
            requestRespondOutbound.setStatusCode(500);
            requestRespondOutbound.setMessage("Error occurred when getting all outbound : " + e.getMessage());
        }
        return requestRespondOutbound;
    }

    @Transactional
    public RequestRespondOutbound registerOutbound(RequestRespondOutbound outboundRegistrationRequest) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        try {
            Optional<Inventory> optionalInventory = inventoryRepo.findBySku(outboundRegistrationRequest.getProductSku());
            if(optionalInventory.isPresent() && (optionalInventory.get().getQuantity() > outboundRegistrationRequest.getQuantity())) {
                Outbound outbound = new Outbound();
                if(Objects.nonNull(outboundRegistrationRequest.getReference()) && !outboundRegistrationRequest.getReference().isEmpty()) {
                    outbound.setReference(outboundRegistrationRequest.getReference());
                } else {
                    long nextId = outboundRepo.count() + 1;
                    String referenceTicket = String.format("OUTBOUND%04d",nextId);
                    outbound.setReference(referenceTicket);
                }
                if(Objects.nonNull(outboundRegistrationRequest.getDateShipped())) {
                    outbound.setDateShipped(outboundRegistrationRequest.getDateShipped());
                } else {
                    outbound.setDateShipped(new Date());
                }
                outbound.setInventory(optionalInventory.get());
                outbound.setQuantity(outboundRegistrationRequest.getQuantity());
                outbound.setDestination(outboundRegistrationRequest.getDestination());
                outbound.setRemarks(outboundRegistrationRequest.getRemarks());
                Outbound outboundRegistered = outboundRepo.save(outbound);
                if(outboundRegistered.getId() > 0) {
                    Inventory updatedInventory = new Inventory();
                    updatedInventory.setId(optionalInventory.get().getId());
                    updatedInventory.setSku(optionalInventory.get().getSku());
                    updatedInventory.setCategory(optionalInventory.get().getCategory());
                    updatedInventory.setName(optionalInventory.get().getName());
                    updatedInventory.setLocation(optionalInventory.get().getLocation());
                    updatedInventory.setQuantity(optionalInventory.get().getQuantity() + outboundRegistered.getQuantity());
                    updatedInventory.setSupplier(optionalInventory.get().getSupplier());
                    Inventory inventoryRegistered = inventoryRepo.save(updatedInventory);
                    if(inventoryRegistered.getId() > 0) {
                        requestRespondOutbound.setStatusCode(200);
                        requestRespondOutbound.setMessage("Outbound has been registered successfully");
                    } else {
                        requestRespondOutbound.setStatusCode(400);
                        requestRespondOutbound.setMessage("Outbound failed to register. Something wrong when calling the repository save");
                    }
                } else {
                    requestRespondOutbound.setStatusCode(400);
                    requestRespondOutbound.setMessage("Outbound failed to register. Maybe wrong SKU or quantity exceed the limit");
                }
            }
        } catch (Exception e) {
            requestRespondOutbound.setStatusCode(500);
            requestRespondOutbound.setMessage(e.getMessage());
        }
        return requestRespondOutbound;
    }

    public RequestRespondOutbound updateOutbound(Long outboundId, RequestRespondOutbound requestUpdateOutbound) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        try {
            Optional<Outbound> optionalOutbound = outboundRepo.findById(outboundId);
            if(optionalOutbound.isPresent()) {
                Outbound outbound = getOutbound(requestUpdateOutbound, optionalOutbound);
                Outbound updateOutbound = outboundRepo.save(outbound);
                if(updateOutbound.getId() > 0) {
                    requestRespondOutbound.setStatusCode(200);
                    requestRespondOutbound.setMessage("Outbound updated successfully");
                }
            } else {
                requestRespondOutbound.setStatusCode(404);
                requestRespondOutbound.setMessage("Outbound not found for update");
            }
        } catch (Exception e) {
            requestRespondOutbound.setStatusCode(500);
            requestRespondOutbound.setMessage("Error occurred when updating outbound :" + e.getMessage());
        }
        return requestRespondOutbound;
    }

    public RequestRespondOutbound deleteOutbound(Long requestOutboundById) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        try {
            Optional<Outbound> outboundOptional = outboundRepo.findById(requestOutboundById);
            if(outboundOptional.isPresent()) {
                outboundRepo.deleteById(requestOutboundById);
                requestRespondOutbound.setStatusCode(200);
                requestRespondOutbound.setMessage("Outbound successfully deleted");
            } else {
                requestRespondOutbound.setStatusCode(404);
                requestRespondOutbound.setMessage("Outbound not found for deletion");
            }
        } catch (Exception e) {
            requestRespondOutbound.setStatusCode(500);
            requestRespondOutbound.setMessage("Error occurred while deleting outbound :" + e.getMessage());
        }
        return requestRespondOutbound;
    }

    private Outbound getOutbound(RequestRespondOutbound requestUpdateOutbound, Optional<Outbound> optionalOutbound) {
        Outbound outbound = optionalOutbound.get();
        if(Objects.nonNull(requestUpdateOutbound.getReference()) && !requestUpdateOutbound.getReference().isEmpty()) {
            outbound.setReference(requestUpdateOutbound.getReference());
        }
        if(Objects.nonNull(requestUpdateOutbound.getDateShipped())) {
            outbound.setDateShipped(requestUpdateOutbound.getDateShipped());
        }
        if(Objects.nonNull(requestUpdateOutbound.getProductSku())) {
            Optional<Inventory> optionalInventory = inventoryRepo.findBySku(requestUpdateOutbound.getProductSku());
            if (optionalInventory.isPresent()) {
                Inventory inventory = optionalInventory.get();
                outbound.setInventory(inventory);
            }
        }
        if(Objects.nonNull(requestUpdateOutbound.getQuantity())) {
            outbound.setQuantity(requestUpdateOutbound.getQuantity());
        }
        if(Objects.nonNull(requestUpdateOutbound.getDestination()) && !requestUpdateOutbound.getDestination().isEmpty()) {
            outbound.setDestination(requestUpdateOutbound.getDestination());
        }
        if(Objects.nonNull(requestUpdateOutbound.getRemarks()) && !requestUpdateOutbound.getRemarks().isEmpty()) {
            outbound.setRemarks(requestUpdateOutbound.getRemarks());
        }
        return outbound;
    }
}
