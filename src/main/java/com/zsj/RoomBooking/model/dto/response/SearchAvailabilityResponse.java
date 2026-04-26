package com.zsj.RoomBooking.model.dto.response;

import java.util.List;

public record SearchAvailabilityResponse(RoomResponse roomResponse, List<TimeRangeResponse> availableSlots) {
}
