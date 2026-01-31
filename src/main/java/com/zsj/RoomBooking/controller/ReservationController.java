package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.entity.Reservation;
import com.zsj.RoomBooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    @Autowired
    private ReservationService service;

    @GetMapping
    public List<Reservation> getReservation() {
        return service.getAllReservations();
    }

    @PostMapping
    public void addReservation(long userId, long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        service.addReservation(userId, roomId, startTime, endTime);
    }
}
