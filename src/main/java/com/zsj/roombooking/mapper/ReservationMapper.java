package com.zsj.roombooking.mapper;

import com.zsj.roombooking.model.entity.Reservation;
import com.zsj.roombooking.model.dto.response.ReservationResponse;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getRoom().getId(),
                reservation.getStartTime(), reservation.getEndTime(),
                reservation.getStatus());
    }
}
