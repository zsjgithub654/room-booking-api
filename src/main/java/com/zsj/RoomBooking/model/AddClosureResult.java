package com.zsj.RoomBooking.model;

import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;

import java.util.List;

public record AddClosureResult(Closure closure, List<Reservation> closedReservations) {

}
