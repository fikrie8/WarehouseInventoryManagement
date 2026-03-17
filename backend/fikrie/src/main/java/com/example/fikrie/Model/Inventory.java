package com.example.fikrie.Model;

import jakarta.persistence.*;
import lombok.Data;

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
    private String category;
    private String name;
    private String location;
    private int quantity;
    private String supplier;
}
