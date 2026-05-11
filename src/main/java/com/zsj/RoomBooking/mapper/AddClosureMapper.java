package com.zsj.RoomBooking.mapper;

import com.zsj.RoomBooking.model.AddClosureResult;
import com.zsj.RoomBooking.model.dto.response.AddClosureResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddClosureMapper {
    @Autowired
    private ClosureMapper closureMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    public AddClosureResponse toResponse(AddClosureResult addClosureResult) {
        return new AddClosureResponse(
                closureMapper.toResponse(addClosureResult.getClosure()),
                addClosureResult.getCanceledReservations().stream().map(reservationMapper::toResponse).toList()
        );
    }
}
