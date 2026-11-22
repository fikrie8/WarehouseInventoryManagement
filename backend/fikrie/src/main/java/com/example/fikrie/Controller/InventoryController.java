package com.example.fikrie.Controller;

import com.example.fikrie.DataTransferObject.RequestRespondInventory;
import com.example.fikrie.Service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/get-inventory")
    public ResponseEntity<RequestRespondInventory> getInventoryInfo(@RequestBody RequestRespondInventory requestRespondInventory) {
        return ResponseEntity.ok(inventoryService.getInventory(requestRespondInventory.getName()));
    }

    @GetMapping("/get-all-inventory")
    public ResponseEntity<RequestRespondInventory> getAllInventoryInfo() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PostMapping("/register-inventory/single-upload")
    public ResponseEntity<RequestRespondInventory> registerSingleInventory(@RequestBody RequestRespondInventory requestRespondInventory) {
        return ResponseEntity.ok(inventoryService.singleRegister(requestRespondInventory));
    }

    @PostMapping("/register-inventory/bulk-upload")
    public ResponseEntity<RequestRespondInventory> registerBulkInventory(@RequestParam("file") MultipartFile file, @RequestParam("headerIndex") int headerIndex) {
        return ResponseEntity.ok(inventoryService.bulkRegister(file, headerIndex));
    }

    @PutMapping("/update-inventory")
    public ResponseEntity<RequestRespondInventory> updateUser(@RequestBody RequestRespondInventory requestUpdateInventory) {
        return ResponseEntity.ok(inventoryService.updateInventory(requestUpdateInventory.getSku(), requestUpdateInventory.getInventory()));
    }

    @DeleteMapping("/delete-inventory/{requestInventoryId}")
    public ResponseEntity<RequestRespondInventory> deleteInventory(@PathVariable(required = true) Integer requestInventoryId) {
        return ResponseEntity.ok(inventoryService.deleteInventory(requestInventoryId));
    }
}
