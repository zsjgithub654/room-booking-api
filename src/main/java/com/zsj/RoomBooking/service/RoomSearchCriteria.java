package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.RoomStatus;

public record RoomSearchCriteria(String name,
                                 Integer minCapacity,
                                 Integer maxCapacity,
                                 String area,
                                 RoomStatus status) {
}
