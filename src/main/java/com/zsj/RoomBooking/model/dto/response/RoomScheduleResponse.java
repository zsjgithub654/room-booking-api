package com.zsj.RoomBooking.model.dto.response;

import java.util.List;

public record RoomScheduleResponse(RoomResponse room,
                                   List<OccupationResponse> occupations) {
}
