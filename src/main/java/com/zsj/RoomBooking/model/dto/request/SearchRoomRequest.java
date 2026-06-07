package com.zsj.RoomBooking.model.dto.request;

import com.zsj.RoomBooking.validation.Range;
import jakarta.validation.constraints.Positive;

@Range(
        minField = "minCapacity",
        maxField = "maxCapacity",
        message = "minimum capacity must be less than or equal to maximum capacity."
)
public record SearchRoomRequest(String name, @Positive Integer minCapacity, @Positive Integer maxCapacity, String area) {
}
