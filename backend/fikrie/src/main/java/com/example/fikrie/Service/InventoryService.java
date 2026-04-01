package com.example.fikrie.Service;

import com.example.fikrie.DataTransferObject.RequestRespondInventory;
import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Repository.InventoryRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class InventoryService {

    @Autowired
    InventoryRepo inventoryRepo;

    public RequestRespondInventory getInventory(String requestInventoryName) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        try {
            Inventory inventory = inventoryRepo.findByName(requestInventoryName);
            requestRespondInventory.setInventory(inventory);
            requestRespondInventory.setStatusCode(200);
            requestRespondInventory.setMessage("Inventory with name : " + requestInventoryName + " found successfully");
        } catch (Exception e) {
            requestRespondInventory.setStatusCode(500);
            requestRespondInventory.setMessage("Error occurred when getting inventory by name : " + e.getMessage());
        }
        return requestRespondInventory;
    }

    public RequestRespondInventory register(RequestRespondInventory inventoryRegistrationRequest) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        try {
            Inventory inventory = new Inventory();
            inventory.setCategory(inventoryRegistrationRequest.getCategory());
            inventory.setSku(inventoryRegistrationRequest.getSku());
            inventory.setName(inventoryRegistrationRequest.getName());
            inventory.setLocation(inventoryRegistrationRequest.getLocation());
            inventory.setQuantity(inventoryRegistrationRequest.getQuantity());
            inventory.setSupplier(inventoryRegistrationRequest.getSupplier());
            Inventory inventoryRegistered = inventoryRepo.save(inventory);
            if(inventoryRegistered.getId() > 0) {
                requestRespondInventory.setInventory(inventoryRegistered);
                requestRespondInventory.setStatusCode(200);
                requestRespondInventory.setMessage("Inventory has been registered successfully");
            }
        } catch (Exception e) {
            requestRespondInventory.setStatusCode(500);
            requestRespondInventory.setMessage(e.getMessage());
        }
        return requestRespondInventory;
    }

    public RequestRespondInventory updateInventory(String sku, Inventory requestUpdateInventory) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        try {
            Optional<Inventory> optionalInventory = inventoryRepo.findBySku(sku);
            if(optionalInventory.isPresent()) {
                Inventory inventory = getInventory(requestUpdateInventory, optionalInventory);
                Inventory savedInventory = inventoryRepo.save(inventory);
                requestRespondInventory.setInventory(savedInventory);
                requestRespondInventory.setStatusCode(200);
                requestRespondInventory.setMessage("Inventory updated successfully");
            } else {
                requestRespondInventory.setStatusCode(404);
                requestRespondInventory.setMessage("Inventory not found for update");
            }
        } catch (Exception e) {
            requestRespondInventory.setStatusCode(500);
            requestRespondInventory.setMessage("Error occurred when updating inventory :" + e.getMessage());
        }
        return requestRespondInventory;
    }

    public RequestRespondInventory deleteInventory(Integer requestInventoryById) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        try {
            Optional<Inventory> inventoryOptional = inventoryRepo.findById(requestInventoryById);
            if(inventoryOptional.isPresent()) {
                inventoryRepo.deleteById(requestInventoryById);
                requestRespondInventory.setStatusCode(200);
                requestRespondInventory.setMessage("Inventory successfully deleted");
            } else {
                requestRespondInventory.setStatusCode(404);
                requestRespondInventory.setMessage("Inventory not found for deletion");
            }
        } catch (Exception e) {
            requestRespondInventory.setStatusCode(500);
            requestRespondInventory.setMessage("Error occurred while deleting user :" + e.getMessage());
        }
        return requestRespondInventory;
    }

    public RequestRespondInventory getAllInventory() {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        try {
            List<Inventory> listOfInventory = inventoryRepo.findAll();
            requestRespondInventory.setListOfInventory(listOfInventory);
            requestRespondInventory.setStatusCode(200);
            requestRespondInventory.setMessage("Inventory updated successfully");
        } catch (Exception e) {
            requestRespondInventory.setStatusCode(500);
            requestRespondInventory.setMessage("Error occurred when updating inventory :" + e.getMessage());
        }
        return requestRespondInventory;
    }

    private Inventory getInventory(Inventory requestUpdateInventory, Optional<Inventory> optionalInventory) {
        Inventory inventory = optionalInventory.get();
        if(Objects.nonNull(requestUpdateInventory.getCategory()) && !requestUpdateInventory.getCategory().isEmpty()) {
            inventory.setCategory(requestUpdateInventory.getCategory());
        }
        if(Objects.nonNull(requestUpdateInventory.getSku()) && !requestUpdateInventory.getSku().isEmpty()) {
            inventory.setSku(requestUpdateInventory.getSku());
        }
        if(Objects.nonNull(requestUpdateInventory.getName()) && !requestUpdateInventory.getName().isEmpty()) {
            inventory.setName(requestUpdateInventory.getName());
        }
        if(Objects.nonNull(requestUpdateInventory.getLocation()) && !requestUpdateInventory.getLocation().isEmpty()) {
            inventory.setLocation(requestUpdateInventory.getLocation());
        }
        if(Objects.nonNull(requestUpdateInventory.getQuantity())) {
            inventory.setQuantity(requestUpdateInventory.getQuantity());
        }
        if(Objects.nonNull(requestUpdateInventory.getSupplier()) && !requestUpdateInventory.getSupplier().isEmpty()) {
            inventory.setSupplier(requestUpdateInventory.getSupplier());
        }
        return inventory;
    }
}
