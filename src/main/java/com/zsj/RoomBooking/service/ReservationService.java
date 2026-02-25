package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    // TODO: what to return
    List<Reservation> getAllReservations();
    Reservation addReservation(long userId, long roomId, LocalDateTime startTime, LocalDateTime endTime);
    Reservation cancelReservation(long reservationId);
    Reservation modifyReservationTime(long reservationId, LocalDateTime startTime, LocalDateTime endTime);
}