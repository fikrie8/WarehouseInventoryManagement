package com.example.fikrie.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Data
@Table( name = "inventory",
        uniqueConstraints={
                @UniqueConstraint(columnNames = "id"),
                @UniqueConstraint(columnNames = "sku")
        })
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private int id;
    @Column(unique = true)
    private String sku;
    @OneToMany(mappedBy = "inventory")
    @JsonManagedReference
    @BatchSize(size = 10)
    private List<Inbound> inboundList;
    @OneToMany(mappedBy = "inventory")
    @JsonManagedReference
    @BatchSize(size = 10)
    private List<Outbound> outboundList;
    private String category;
    private String name;
    private String location;
    private int quantity;
    private String supplier;
}
