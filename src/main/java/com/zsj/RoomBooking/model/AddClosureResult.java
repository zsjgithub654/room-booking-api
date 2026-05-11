package com.zsj.RoomBooking.model;

import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;

import java.util.List;

public class AddClosureResult {
    private final Closure closure;
    private final List<Reservation> canceledReservations;

    public Closure getClosure() {
        return closure;
    }

    public List<Reservation> getCanceledReservations() {
        return canceledReservations;
    }

    public AddClosureResult(Closure closure, List<Reservation> canceledReservations) {
        this.closure = closure;
        this.canceledReservations = canceledReservations;
    }
}
