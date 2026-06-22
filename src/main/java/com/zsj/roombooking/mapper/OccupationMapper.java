package com.zsj.roombooking.mapper;

import com.zsj.roombooking.model.Occupation;
import com.zsj.roombooking.model.dto.response.OccupationResponse;
import org.springframework.stereotype.Component;

@Component
public class OccupationMapper {
    public OccupationResponse toResponse(Occupation occupation) {
        return new OccupationResponse(
                occupation.getOccupationType(),
                occupation.getId(),
                occupation.getStartTime(),
                occupation.getEndTime());
    }
}
