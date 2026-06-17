package com.zsj.RoomBooking.model.dto.response;

import com.zsj.RoomBooking.model.RoomStatus;

import java.time.LocalTime;

public record RoomResponse(Long id,
                           String name,
                           Integer capacity,
                           String area,
                           LocalTime openTime, LocalTime closeTime,
                           RoomStatus status) {
}