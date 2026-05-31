package com.zsj.RoomBooking.mapper;

import com.zsj.RoomBooking.model.Occupation;
import com.zsj.RoomBooking.model.dto.response.OccupationResponse;
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
