package com.zsj.RoomBooking.mapper;

import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
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

    public Reservation toEntity(ReservationResponse response) {
        return null;
    }
}
