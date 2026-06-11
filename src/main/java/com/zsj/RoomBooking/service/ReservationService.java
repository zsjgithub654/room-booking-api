package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReservationService {
    Page<Reservation> searchReservations(Long userId, Long roomId, LocalDate date, ReservationStatus status, Pageable pageable);
    Reservation getReservation(Long id);
    Reservation addReservation(Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteReservation(Long reservationId);
    Reservation updateReservationTime(Long id, LocalDateTime startTime, LocalDateTime endTime);
}