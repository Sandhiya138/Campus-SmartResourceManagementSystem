package com.example.demo.repository;
import com.example.demo.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // resources that are free in the given time range
    @Query("select r from Resource r where r.availability = true and r.id not in " +
           "(select b.resourceId from Booking b where b.status = 'BOOKED' " +
           "and b.startTime < :end and b.endTime > :start)")
    List<Resource> findAvailable(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
