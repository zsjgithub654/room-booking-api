package com.zsj.roombooking.model.dto.response;

import com.zsj.roombooking.model.OccupationType;

import java.time.LocalDateTime;

public record OccupationResponse(OccupationType type,
                                 Long id,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime) {
}
