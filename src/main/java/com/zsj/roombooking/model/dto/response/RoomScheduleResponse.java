package com.zsj.roombooking.model.dto.response;

import java.util.List;

public record RoomScheduleResponse(RoomResponse room,
                                   List<OccupationResponse> occupations) {
}
