package com.zsj.RoomBooking.model;

import java.util.List;

public record AddClosureResponse(ClosureResponse closure, List<ReservationResponse> canceledReservations) {
}
