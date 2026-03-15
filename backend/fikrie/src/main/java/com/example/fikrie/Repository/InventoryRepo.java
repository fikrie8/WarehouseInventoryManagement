package com.example.fikrie.Repository;

import com.example.fikrie.Model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepo extends JpaRepository<Inventory, Integer> {

    Inventory findByName(String name);
    Optional<Inventory> findBySku(String sku);
}
