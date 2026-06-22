package com.zsj.roombooking.model.dto.response;

import com.zsj.roombooking.model.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(Long id,
                                  Long userId,
                                  Long roomId,
                                  LocalDateTime startTime,
                                  LocalDateTime endTime,
                                  ReservationStatus status) {
}
