package com.zsj.roombooking.model.dto.request;

import com.zsj.roombooking.model.RoomStatus;
import com.zsj.roombooking.validation.Range;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Range(
        minField = "minCapacity",
        maxField = "maxCapacity",
        message = "{room.capacity.range}"
)
public record SearchRoomRequest(@Size(max = 50) String name,
                                @Positive Integer minCapacity,
                                @Positive Integer maxCapacity,
                                @Size(max = 50) String area,
                                RoomStatus status) {
}
