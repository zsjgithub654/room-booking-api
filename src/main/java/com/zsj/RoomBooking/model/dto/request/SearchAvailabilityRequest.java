package com.zsj.RoomBooking.model.dto.request;

import java.time.LocalDateTime;

public record SearchAvailabilityRequest(String name,
                                        Integer minCapacity, Integer maxCapacity, /* use wrapper to accept null */
                                            String area,
                                        LocalDateTime startTime, LocalDateTime endTime) {
}