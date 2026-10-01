package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(unique = true)
    public String username;
    @JsonIgnore  // never send password to frontend
    public String password;
    public String role;    // STUDENT, FACULTY, ADMIN
    public String status;  // PENDING, APPROVED, REJECTED
}

