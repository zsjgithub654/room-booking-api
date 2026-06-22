package com.zsj.roombooking.model.dto.response;

import java.time.LocalDateTime;

public record TimeRangeResponse(LocalDateTime startTime,
                                LocalDateTime endTime) {
}
