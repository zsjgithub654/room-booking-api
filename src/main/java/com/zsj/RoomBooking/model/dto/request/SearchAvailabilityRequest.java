package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.MaxDurationDays;
import com.zsj.RoomBooking.validation.Range;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Range(
        fromField = "minCapacity",
        toField = "maxCapacity",
        allowEqual = true,
        message = "minimum capacity must be less than or equal to maximum capacity."
)
@MaxDurationDays(startField = "startTime", endField = "endTime", days = 7)
@Range(fromField = "startTime", toField = "endTime", message = "start time must be before end time.")
public record SearchAvailabilityRequest(String name,
                                        @Positive Integer minCapacity, @Positive Integer maxCapacity, /* use wrapper to accept null */
                                        String area,
                                        @NotNull @Future LocalDateTime startTime,
                                        @NotNull @Future LocalDateTime endTime) {
}
