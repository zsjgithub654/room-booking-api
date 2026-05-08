package com.zsj.RoomBooking.mapper;

import com.zsj.RoomBooking.model.Availability;
import com.zsj.RoomBooking.model.dto.response.AvailabilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityMapper {
    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private TimeRangeMapper timeRangeMapper;

    public AvailabilityResponse toResponse(Availability availability) {
        return new AvailabilityResponse(
                roomMapper.toResponse(availability.getRoom()),
                availability.getAvailableSlots().stream().map(timeRangeMapper::toResponse).toList());
    }
}
