package com.zsj.roombooking.mapper;

import com.zsj.roombooking.model.dto.response.ClosureResponse;
import com.zsj.roombooking.model.entity.Closure;
import org.springframework.stereotype.Component;

@Component
public class ClosureMapper {
    public ClosureResponse toResponse(Closure closure) {
        return new ClosureResponse(
                closure.getId(),
                closure.getRoom().getId(),
                closure.getStartTime(), closure.getEndTime());
    }
}
