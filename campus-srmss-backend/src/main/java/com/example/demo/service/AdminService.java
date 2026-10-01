package com.example.demo.service;

import com.example.demo.entity.Resource;
import com.example.demo.entity.User;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    @Autowired UserRepository users;
    @Autowired ResourceRepository resources;
    @Autowired BookingRepository bookings;
    @Autowired AuditService audit;

    public List<User> allUsers() {
        return users.findAll();
    }

    public User setStatus(String adminName, Long id, String status) {
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status))
            throw new ApiException(400, "Status must be APPROVED or REJECTED");
        User u = users.findById(id).orElseThrow(() -> new ApiException(404, "User not found"));
        u.status = status;
        users.save(u);
        User admin = users.findByUsername(adminName).get();
        audit.log(admin.id, "USER_" + status + " #" + id);
        return u;
    }

    public List<Map<String, Object>> dailyReport(LocalDate date) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : bookings.dailyReport(date.atStartOfDay(), date.plusDays(1).atStartOfDay())) {
            Resource r = resources.findById((Long) row[0]).orElse(null);
            result.add(Map.of("resource", r == null ? "deleted" : r.name, "bookings", row[1]));
        }
        return result;
    }
}
