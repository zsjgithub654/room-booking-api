package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    // TODO: what to return
    public List<Reservation> getAllReservations();
    public Reservation addReservation(long userId, long roomId, LocalDateTime startTime, LocalDateTime endTime);
    public Reservation cancelReservation(long reservationId);
    public Reservation modifyReservationTime(long reservationId, LocalDateTime startTime, LocalDateTime endTime);
}
