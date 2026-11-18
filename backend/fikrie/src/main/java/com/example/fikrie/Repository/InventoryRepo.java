package com.example.fikrie.Repository;

import com.example.fikrie.DataTransferObject.InventoryDTO;
import com.example.fikrie.Model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepo extends JpaRepository<Inventory, Integer> {

    Optional<Inventory> findByName(String name);
    Optional<Inventory> findBySku(String sku);
    List<InventoryDTO> findAllBy();
}
