package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.model.ReservationStatus;

import java.time.LocalDate;

public record SearchReservationRequest(Long userId, Long roomId, LocalDate date, ReservationStatus status) {
}
