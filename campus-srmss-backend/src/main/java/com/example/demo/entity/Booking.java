package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long userId;
    public Long resourceId;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public String status;   // BOOKED, CANCELLED
}