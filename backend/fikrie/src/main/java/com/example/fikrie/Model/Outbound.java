package com.example.fikrie.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table( name = "outbound",
        uniqueConstraints={
                @UniqueConstraint(columnNames = "id"),
                @UniqueConstraint(columnNames = "reference")
        })
public class Outbound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private int id;
    @Column(unique = true)
    private String reference;
    private Date dateShipped;
    @ManyToOne
    @JoinColumn(name = "product_sku", referencedColumnName = "sku", nullable = false)
    private Inventory inventory;
    private int quantity;
    private String destination;
    private String remarks;
}
