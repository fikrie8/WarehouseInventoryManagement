package com.example.fikrie.DataTransferObject;

public record InventoryDTO(
        String category,
        String sku,
        String name,
        String location,
        int quantity,
        String supplier
) { }
