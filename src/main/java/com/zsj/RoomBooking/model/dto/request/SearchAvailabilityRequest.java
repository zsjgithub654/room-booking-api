package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.TimeRange;

import java.time.LocalDateTime;

@TimeRange(startField = "startTime", endField = "endTime")
public record SearchAvailabilityRequest(String name,
                                        Integer minCapacity, Integer maxCapacity, /* use wrapper to accept null */
                                        String area,
                                        LocalDateTime startTime, LocalDateTime endTime) {
}