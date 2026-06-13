package com.zsj.RoomBooking.model.dto.response;

import com.zsj.RoomBooking.model.OccupationType;

import java.time.LocalDateTime;

public record OccupationResponse(OccupationType type,
                                 Long id,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime) {
}
