package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.MaxDurationDays;
import com.zsj.RoomBooking.validation.TimeInterval;
import com.zsj.RoomBooking.validation.Range;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Range(
        minField = "minCapacity",
        maxField = "maxCapacity",
        message = "minimum capacity must be less than or equal to maximum capacity."
)
@MaxDurationDays(startField = "startDate", endField = "endDate", days = 7)
@TimeInterval(fromField = "startDate", toField = "endDate", allowEqual = true,
        message = "start date must be before or equal to end date.")
public record SearchAvailabilityRequest(@Size(max = 50) String name,
                                        @Positive Integer minCapacity,
                                        @Positive Integer maxCapacity, /* use wrapper to accept null */
                                        @Size(max = 50) String area,
                                        @NotNull @FutureOrPresent LocalDate startDate,
                                        @NotNull @FutureOrPresent LocalDate endDate,
                                        Boolean includeUnavailable) {
}

