package com.example.fikrie.Service;

import com.example.fikrie.DataTransferObject.InventoryDTO;
import com.example.fikrie.DataTransferObject.RequestRespondInventory;
import com.example.fikrie.Exception.ResourceNotFoundException;
import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Repository.InventoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepo inventoryRepo;

    public RequestRespondInventory getInventory(String requestInventoryName) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        Inventory inventory = inventoryRepo.findByName(requestInventoryName)
                .orElseThrow(() -> new ResourceNotFoundException("getInventory failed: Inventory not found with name: " + requestInventoryName));
        requestRespondInventory.setInventory(inventory);
        requestRespondInventory.setStatusCode(200);
        requestRespondInventory.setMessage("Inventory with name : " + requestInventoryName + " found successfully");

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

            Inventory inventoryInDb = inventoryRepo.findBySku(sku)
                    .orElseThrow(() -> new ResourceNotFoundException("updateInventory failed: Inventory not found by SKU: " + sku));

            Inventory updatedinventory = getInventoryFields(requestUpdateInventory, inventoryInDb);
            Inventory savedInventory = inventoryRepo.save(updatedinventory);
            requestRespondInventory.setInventory(savedInventory);
            requestRespondInventory.setStatusCode(200);
            requestRespondInventory.setMessage("Inventory updated successfully");

        return requestRespondInventory;
    }

    @Transactional
    public RequestRespondInventory deleteInventory(Integer requestInventoryById) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        Inventory inventory = inventoryRepo.findById(requestInventoryById)
                .orElseThrow(() -> new ResourceNotFoundException("deleteInventory failed: Inventory not found by Id: " + requestInventoryById));
        inventoryRepo.deleteById(requestInventoryById);
        requestRespondInventory.setStatusCode(200);
        requestRespondInventory.setMessage("Inventory successfully deleted");

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

    private Inventory getInventoryFields(Inventory requestUpdateInventory, Inventory inventory) {
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
