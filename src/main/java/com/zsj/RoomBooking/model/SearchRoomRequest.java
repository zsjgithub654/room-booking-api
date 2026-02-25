package com.zsj.RoomBooking.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record SearchRoomRequest(String name,
                                Integer minCapacity, Integer maxCapacity, /* use wrapper to accept null */
                                String area,
                                LocalDate date, /* not null */
                                LocalTime fromTime, LocalTime toTime) { /* nullable */
}