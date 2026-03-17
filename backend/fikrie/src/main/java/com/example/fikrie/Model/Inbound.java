package com.example.fikrie.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table( name = "inbound",
        uniqueConstraints={
                @UniqueConstraint(columnNames = "id"),
                @UniqueConstraint(columnNames = "reference")
        })
public class Inbound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private int id;
    @Column(unique = true)
    private String reference;
    private Date dateReceived;
    @ManyToOne
    @JoinColumn(name = "product_sku", referencedColumnName = "sku", nullable = false)
    private Inventory inventory;
    private int quantity;
    private String location;
    private String remarks;
}
