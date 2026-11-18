package com.example.fikrie.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table( name = "users",
        uniqueConstraints={
        @UniqueConstraint(columnNames = "id"),
        @UniqueConstraint(columnNames = "username")
})
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private int id;
    @Column(unique = true)
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String department;
    private String role;
    private String email;
}
