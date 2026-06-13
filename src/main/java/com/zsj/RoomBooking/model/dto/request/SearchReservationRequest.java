package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.model.ReservationStatus;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record SearchReservationRequest(@Positive Long userId,
                                       @Positive Long roomId,
                                       LocalDate date,
                                       ReservationStatus status) {
}
