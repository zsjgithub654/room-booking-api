package com.zsj.RoomBooking.mapper;

import com.zsj.RoomBooking.model.dto.response.ClosureResponse;
import com.zsj.RoomBooking.model.entity.Closure;
import org.springframework.stereotype.Component;

@Component
public class ClosureMapper {
    public ClosureResponse toResponse(Closure closure) {
        return new ClosureResponse(
                closure.getId(),
                closure.getUser().getId(),
                closure.getRoom().getId(),
                closure.getStartTime(), closure.getEndTime());
    }
}
