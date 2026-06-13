package com.zsj.RoomBooking.model.dto.response;

import java.util.List;

public record DeleteRoomResponse(Long roomId,
                                 List<ReservationResponse> closedReservations) {
}
