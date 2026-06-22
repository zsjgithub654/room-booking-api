package com.zsj.roombooking.model.dto.response;

import java.time.LocalDateTime;

public record ClosureResponse(Long id,
                              Long roomId,
                              LocalDateTime startTime,
                              LocalDateTime endTime) {
}