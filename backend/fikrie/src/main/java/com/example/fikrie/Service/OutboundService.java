package com.example.fikrie.Service;

import com.example.fikrie.DataTransferObject.RequestRespondOutbound;
import com.example.fikrie.Exception.ResourceNotFoundException;
import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Model.Outbound;
import com.example.fikrie.Repository.InventoryRepo;
import com.example.fikrie.Repository.OutboundRepo;
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
public class OutboundService {

    private final OutboundRepo outboundRepo;
    private final InventoryRepo inventoryRepo;

    public RequestRespondOutbound getOutboundByReference(String requestOutboundReference) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        Outbound outbound = outboundRepo.findByReference(requestOutboundReference)
                .orElseThrow(() -> new ResourceNotFoundException("getOutboundByReference failed: Outbound not found by Reference: " + requestOutboundReference));
        requestRespondOutbound.setReference(outbound.getReference());
        requestRespondOutbound.setDateShipped(outbound.getDateShipped());
        requestRespondOutbound.setProductSku(outbound.getInventory().getSku());
        requestRespondOutbound.setQuantity(outbound.getQuantity());
        requestRespondOutbound.setDestination(outbound.getDestination());
        requestRespondOutbound.setRemarks(outbound.getRemarks());
        requestRespondOutbound.setStatusCode(200);
        requestRespondOutbound.setMessage("Outbound with reference : " + requestOutboundReference + " found successfully");

        return requestRespondOutbound;
    }

    public RequestRespondOutbound getOutboundBySku(String requestOutboundSku) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        List<Outbound> listOfOutbounds = outboundRepo.findByInventorySku(requestOutboundSku);
        if(listOfOutbounds.isEmpty()) {
            throw new ResourceNotFoundException("getOutboundBySku failed: Outbounds not found by Sku: " + requestOutboundSku);
        }
        requestRespondOutbound.setListOfOutbound(listOfOutbounds);
        requestRespondOutbound.setStatusCode(200);
        requestRespondOutbound.setMessage("Outbound with product_sku : " + requestOutboundSku + " found successfully");

        return requestRespondOutbound;
    }

    public RequestRespondOutbound getAllOutbound() {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        List<Outbound> listOfOutbounds = outboundRepo.findAll();
        if(listOfOutbounds.isEmpty()) {
            throw new ResourceNotFoundException("getAllOutbound failed: No outbounds record is found in the system");
        }
        requestRespondOutbound.setListOfOutbound(listOfOutbounds);
        requestRespondOutbound.setStatusCode(200);
        requestRespondOutbound.setMessage("All outbound found successfully");

        return requestRespondOutbound;
    }

    @Transactional
    public RequestRespondOutbound registerOutbound(RequestRespondOutbound outboundRegistrationRequest) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        if(outboundRegistrationRequest.getQuantity() <= 0) {
            throw new DataIntegrityViolationException("RegisterOutbound failed: Outbound quantity cannot be lower than or equal to 0");
        }
        Inventory inventory = inventoryRepo.findBySku(outboundRegistrationRequest.getProductSku())
                .orElseThrow(() -> new ResourceNotFoundException("RegisterOutBound failed: Inventory not found by Sku: " + outboundRegistrationRequest.getProductSku()));
        if(inventory.getQuantity() < outboundRegistrationRequest.getQuantity()) {
            throw new DataIntegrityViolationException("RegisterOutbound failed: Insufficient stock available in inventory");
        }
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
        outbound.setInventory(inventory);
        outbound.setQuantity(outboundRegistrationRequest.getQuantity());
        outbound.setDestination(outboundRegistrationRequest.getDestination());
        outbound.setRemarks(outboundRegistrationRequest.getRemarks());
        Outbound outboundRegistered = outboundRepo.save(outbound);
        inventory.setQuantity(inventory.getQuantity() - outboundRegistered.getQuantity());
        requestRespondOutbound.setStatusCode(200);
        requestRespondOutbound.setMessage("Outbound has been registered successfully");

        return requestRespondOutbound;
    }

    public RequestRespondOutbound updateOutbound(Long outboundId, RequestRespondOutbound requestUpdateOutbound) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        if(requestUpdateOutbound.getQuantity() <= 0) {
            throw new DataIntegrityViolationException("updateOutbound failed: Outbound quantity cannot be lower than or equals to 0");
        }
        Outbound oldOutbound = outboundRepo.findById(outboundId)
                .orElseThrow(() -> new ResourceNotFoundException("updateOutbound failed: Outbound not found by Id: " + outboundId));
        Inventory oldInventory = oldOutbound.getInventory();
        oldInventory.setQuantity(oldInventory.getQuantity() + oldOutbound.getQuantity());
        Outbound updatedOutbound = getOutboundFields(requestUpdateOutbound, oldOutbound);
        Outbound updateOutbound = outboundRepo.save(updatedOutbound);
        Inventory newInventory = updatedOutbound.getInventory();
        newInventory.setQuantity(newInventory.getQuantity() - updateOutbound.getQuantity());
        requestRespondOutbound.setStatusCode(200);
        requestRespondOutbound.setMessage("Outbound updated successfully");

        return requestRespondOutbound;
    }

    public RequestRespondOutbound deleteOutbound(Long requestOutboundById) {
        RequestRespondOutbound requestRespondOutbound = new RequestRespondOutbound();
        Outbound outbound = outboundRepo.findById(requestOutboundById)
                .orElseThrow(() -> new ResourceNotFoundException("DeleteOutbound failed: Outbound not found by Id: " + requestOutboundById));
        Inventory inventory = outbound.getInventory();
        int outboundQuantity = outbound.getQuantity();
        outboundRepo.deleteById(requestOutboundById);
        inventory.setQuantity(inventory.getQuantity() + outboundQuantity);
        requestRespondOutbound.setStatusCode(200);
        requestRespondOutbound.setMessage("Outbound successfully deleted");

        return requestRespondOutbound;
    }

    private Outbound getOutboundFields(RequestRespondOutbound requestUpdateOutbound, Outbound outbound) {
        if (Objects.nonNull(requestUpdateOutbound.getReference()) && !requestUpdateOutbound.getReference().isEmpty()) {
            outbound.setReference(requestUpdateOutbound.getReference());
        }
        if (Objects.nonNull(requestUpdateOutbound.getDateShipped())) {
            outbound.setDateShipped(requestUpdateOutbound.getDateShipped());
        }
        if (Objects.nonNull(requestUpdateOutbound.getProductSku()) && !requestUpdateOutbound.getProductSku().isEmpty()) {
            Inventory inventory = inventoryRepo.findBySku(requestUpdateOutbound.getProductSku())
                    .orElseThrow(() -> new ResourceNotFoundException("getOutbound failed: Inventory not found by Sku: " + requestUpdateOutbound.getProductSku()));
            outbound.setInventory(inventory);
        }
        if (requestUpdateOutbound.getQuantity() > 0) {
            Inventory currentInventory = outbound.getInventory();
            if (requestUpdateOutbound.getQuantity() > currentInventory.getQuantity()) {
                throw new DataIntegrityViolationException("getOutboundFields failed: Outbound quantity cannot exceed inventory quantity");
            }
            outbound.setQuantity(requestUpdateOutbound.getQuantity());
        }
        if (Objects.nonNull(requestUpdateOutbound.getDestination()) && !requestUpdateOutbound.getDestination().isEmpty()) {
            outbound.setDestination(requestUpdateOutbound.getDestination());
        }
        if (Objects.nonNull(requestUpdateOutbound.getRemarks()) && !requestUpdateOutbound.getRemarks().isEmpty()) {
            outbound.setRemarks(requestUpdateOutbound.getRemarks());
        }

        return outbound;
    }
}
