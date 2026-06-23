package com.zsj.roombooking.model.result;

import com.zsj.roombooking.model.entity.Closure;
import com.zsj.roombooking.model.entity.Reservation;

import java.util.List;

public record AddClosureResult(Closure closure,
                               List<Reservation> affectedReservations) {
}
