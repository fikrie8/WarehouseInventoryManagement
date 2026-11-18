package com.example.fikrie.Repository;

import com.example.fikrie.Model.Outbound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutboundRepo extends JpaRepository<Outbound, Long> {

    Optional<Outbound> findByReference(String name);
    List<Outbound> findByInventorySku(String sku);
    @Query("SELECT o FROM Outbound o JOIN FETCH o.inventory")
    List<Outbound> findAllWithInventory();
}
