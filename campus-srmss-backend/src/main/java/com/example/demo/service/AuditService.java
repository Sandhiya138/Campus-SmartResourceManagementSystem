package com.example.demo.service;
import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    @Autowired
    AuditLogRepository repo;

    public void log(Long userId, String action) {
        AuditLog a = new AuditLog();
        a.userId = userId;
        a.action = action;
        a.timestamp = LocalDateTime.now();
        repo.save(a);
    }

    public List<AuditLog> filter(Long userId, LocalDateTime from, LocalDateTime to) {
        return repo.filter(userId, from, to);
    }
}