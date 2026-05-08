package com.zsj.RoomBooking.model.dto.response;

import java.util.List;

public record AvailabilityResponse(RoomResponse room, List<TimeRangeResponse> availableSlots) {
}
