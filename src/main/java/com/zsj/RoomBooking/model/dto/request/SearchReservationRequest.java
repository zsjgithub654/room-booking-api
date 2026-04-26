package com.zsj.RoomBooking.model.dto.request;

import java.time.LocalDate;

public record SearchReservationRequest(Long userId, Long roomId, LocalDate date) {
}
