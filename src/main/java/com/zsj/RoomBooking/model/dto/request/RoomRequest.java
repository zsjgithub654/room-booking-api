package com.zsj.RoomBooking.model.dto.request;

import java.time.LocalTime;

public record RoomRequest(String name, int capacity, String area, LocalTime openTime, LocalTime closeTime) {
}