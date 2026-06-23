package com.zsj.roombooking.mapper;

import com.zsj.roombooking.model.result.AddClosureResult;
import com.zsj.roombooking.model.dto.response.AddClosureResponse;
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
                closureMapper.toResponse(addClosureResult.closure()),
                addClosureResult.affectedReservations().stream().map(reservationMapper::toResponse).toList()
        );
    }
}
