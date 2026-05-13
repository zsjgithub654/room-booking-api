package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    List<Reservation> searchReservations(Long userId, Long roomId, LocalDate date, ReservationStatus status);
    Reservation getReservation(Long id);
    Reservation addReservation(Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteReservation(Long reservationId);
    Reservation updateReservationTime(Long id, LocalDateTime startTime, LocalDateTime endTime);
}