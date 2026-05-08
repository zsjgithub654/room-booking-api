package com.zsj.RoomBooking.mapper;

import com.zsj.RoomBooking.model.TimeRange;
import com.zsj.RoomBooking.model.dto.response.TimeRangeResponse;
import org.springframework.stereotype.Component;

@Component
public class TimeRangeMapper {
    public TimeRangeResponse toResponse(TimeRange timeRange) {
        return new TimeRangeResponse(timeRange.getStartTime(), timeRange.getEndTime());
    }
}
