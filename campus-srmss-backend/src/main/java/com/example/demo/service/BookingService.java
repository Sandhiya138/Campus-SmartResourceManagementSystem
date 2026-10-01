package com.example.demo.service;

import com.example.demo.entity.Booking;
import com.example.demo.entity.Resource;
import com.example.demo.entity.User;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {
    @Autowired BookingRepository bookings;
    @Autowired UserRepository users;
    @Autowired ResourceRepository resources;
    @Autowired AuditService audit;
    @Autowired NotificationService notifier;

    private User getUser(String username) {
        return users.findByUsername(username).orElseThrow(() -> new ApiException(401, "User not found"));
    }

    private Booking getBooking(Long id) {
        return bookings.findById(id).orElseThrow(() -> new ApiException(404, "Booking not found"));
    }

    private void checkOwner(User u, Booking b) {
        if (!"ADMIN".equals(u.role) && !b.userId.equals(u.id))
            throw new ApiException(403, "This is not your booking");
        if ("CANCELLED".equals(b.status))
            throw new ApiException(400, "Booking is already cancelled");
    }

    private LocalDateTime[] readTimes(Map<String, Object> body) {
        try {
            LocalDateTime s = LocalDateTime.parse(String.valueOf(body.get("startTime")));
            LocalDateTime e = LocalDateTime.parse(String.valueOf(body.get("endTime")));
            if (!e.isAfter(s)) throw new ApiException(400, "End time must be after start time");
            return new LocalDateTime[]{s, e};
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(400, "Invalid date/time format");
        }
    }

    public Booking create(String username, Map<String, Object> body) {
        User u = getUser(username);
        LocalDateTime[] t = readTimes(body);
        Long rid;
        try {
            rid = Long.valueOf(String.valueOf(body.get("resourceId")));
        } catch (Exception e) {
            throw new ApiException(400, "Invalid resource");
        }
        Resource r = resources.findById(rid).orElseThrow(() -> new ApiException(404, "Resource not found"));
        if (!r.availability) throw new ApiException(400, "Resource is not available");
        if ("STUDENT".equals(u.role) && "CLASSROOM".equals(r.type))
            throw new ApiException(403, "Students cannot book classrooms");
        if (bookings.countOverlap(rid, t[0], t[1], 0L) > 0)
            throw new ApiException(409, "This resource is already booked for that time");

        Booking b = new Booking();
        b.userId = u.id;
        b.resourceId = rid;
        b.startTime = t[0];
        b.endTime = t[1];
        b.status = "BOOKED";
        bookings.save(b);
        audit.log(u.id, "BOOKING_CREATED #" + b.id);
        notifier.send(u.username, "Booking confirmed: " + r.name + " from " + t[0] + " to " + t[1]);
        return b;
    }

    public Booking modify(String username, Long id, Map<String, Object> body) {
        User u = getUser(username);
        Booking b = getBooking(id);
        checkOwner(u, b);
        LocalDateTime[] t = readTimes(body);
        if (bookings.countOverlap(b.resourceId, t[0], t[1], b.id) > 0)
            throw new ApiException(409, "This resource is already booked for that time");
        b.startTime = t[0];
        b.endTime = t[1];
        bookings.save(b);
        audit.log(u.id, "BOOKING_MODIFIED #" + b.id);
        notifier.send(u.username, "Booking #" + b.id + " changed to " + t[0] + " - " + t[1]);
        return b;
    }

    public Booking cancel(String username, Long id) {
        User u = getUser(username);
        Booking b = getBooking(id);
        checkOwner(u, b);
        b.status = "CANCELLED";
        bookings.save(b);
        audit.log(u.id, "BOOKING_CANCELLED #" + b.id);
        notifier.send(u.username, "Booking #" + b.id + " cancelled");
        return b;
    }

    public List<Booking> list(String username) {
        User u = getUser(username);
        if ("ADMIN".equals(u.role)) return bookings.findAll();
        return bookings.findByUserIdOrderByStartTimeDesc(u.id);
    }

    public List<Booking> slots(Long resourceId, LocalDate date) {
        return bookings.findByResourceIdAndStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                resourceId, "BOOKED", date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}
