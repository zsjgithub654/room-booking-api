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
        message = "{room.capacity.range}"
)
@MaxDurationDays(startField = "startDate", endField = "endDate", days = 7)
@TimeInterval(fromField = "startDate", toField = "endDate", allowEqual = true,
        message = "{availability.date-range.time-interval}")
public record SearchAvailabilityRequest(@Size(max = 50) String name,
                                        @Positive Integer minCapacity,
                                        @Positive Integer maxCapacity, /* use wrapper to accept null */
                                        @Size(max = 50) String area,
                                        @NotNull @FutureOrPresent LocalDate startDate,
                                        @NotNull @FutureOrPresent LocalDate endDate,
                                        Boolean includeUnavailable) {
}
