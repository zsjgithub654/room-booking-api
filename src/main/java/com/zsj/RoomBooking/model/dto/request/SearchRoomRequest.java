package com.zsj.RoomBooking.model.dto.request;

public record SearchRoomRequest(String name, Integer minCapacity, Integer maxCapacity, String area) {
}
