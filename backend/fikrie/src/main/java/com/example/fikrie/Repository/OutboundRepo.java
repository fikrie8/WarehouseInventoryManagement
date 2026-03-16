package com.example.fikrie.Repository;

import com.example.fikrie.Model.Outbound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboundRepo extends JpaRepository<Outbound, Long> {

    Outbound findByReference(String name);
    List<Outbound> findByInventorySku(String sku);
}
