package com.example.demo.repository;
import com.example.demo.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // SQL query 3: audit logs filtered by user and/or date (both optional)
    @Query("select a from AuditLog a where (:userId is null or a.userId = :userId) " +
           "and (:from is null or a.timestamp >= :from) and (:to is null or a.timestamp < :to) " +
           "order by a.timestamp desc")
    List<AuditLog> filter(@Param("userId") Long userId, @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to);
}
