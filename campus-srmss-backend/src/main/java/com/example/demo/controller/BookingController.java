package com.example.demo.controller;

import com.example.demo.entity.Booking;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired BookingService service;

    @PostMapping
    public Booking create(Authentication auth, @RequestBody Map<String, Object> body) {
        return service.create(auth.getName(), body);
    }

    @GetMapping
    public List<Booking> list(Authentication auth) {
        return service.list(auth.getName());
    }

    @PutMapping("/{id}")
    public Booking modify(Authentication auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return service.modify(auth.getName(), id, body);
    }

    @DeleteMapping("/{id}")
    public Booking cancel(Authentication auth, @PathVariable Long id) {
        return service.cancel(auth.getName(), id);
    }

    // used by the calendar: booked slots of one resource on one day
    @GetMapping("/slots")
    public List<Booking> slots(@RequestParam Long resourceId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.slots(resourceId, date);
    }
}
