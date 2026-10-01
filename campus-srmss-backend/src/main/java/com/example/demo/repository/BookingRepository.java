package com.example.demo.repository;
import com.example.demo.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // SQL query 1: detect overlapping bookings
    @Query("select count(b) from Booking b where b.resourceId = :rid and b.status = 'BOOKED' " +
           "and b.startTime < :end and b.endTime > :start and b.id <> :excludeId")
    long countOverlap(@Param("rid") Long rid, @Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end, @Param("excludeId") Long excludeId);

    List<Booking> findByUserIdOrderByStartTimeDesc(Long userId);

    List<Booking> findByResourceIdAndStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long resourceId, String status, LocalDateTime from, LocalDateTime to);

    // SQL query 2: daily utilization report (bookings per resource for one day)
    @Query("select b.resourceId, count(b) from Booking b where b.status = 'BOOKED' " +
           "and b.startTime >= :from and b.startTime < :to group by b.resourceId")
    List<Object[]> dailyReport(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
