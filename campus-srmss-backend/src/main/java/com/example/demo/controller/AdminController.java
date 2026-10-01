package com.example.demo.controller;
import com.example.demo.entity.User;
import com.example.demo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired AdminService service;

    @GetMapping("/users")
    public List<User> users() {
        return service.allUsers();
    }

    @PutMapping("/users/{id}")
    public User setStatus(Authentication auth, @PathVariable Long id, @RequestParam String status) {
        return service.setStatus(auth.getName(), id, status);
    }

    @GetMapping("/report")
    public List<Map<String, Object>> report(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.dailyReport(date);
    }
}
