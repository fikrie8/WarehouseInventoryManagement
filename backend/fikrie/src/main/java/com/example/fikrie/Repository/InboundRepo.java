package com.example.fikrie.Repository;

import com.example.fikrie.Model.Inbound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InboundRepo extends JpaRepository<Inbound, Long> {

    Optional<Inbound> findByReference(String name);
    List<Inbound> findByInventorySku(String sku);
}
