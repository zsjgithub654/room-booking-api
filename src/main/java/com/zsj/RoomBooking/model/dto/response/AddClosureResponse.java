package com.zsj.RoomBooking.model.dto.response;

import java.util.List;

public record AddClosureResponse(ClosureResponse closure, List<ReservationResponse> canceledReservations) {
}
