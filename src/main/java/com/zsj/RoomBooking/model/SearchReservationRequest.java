package com.zsj.RoomBooking.model;

import java.time.LocalDate;

public record SearchReservationRequest(Long userId, Long roomId, LocalDate date) {
}
