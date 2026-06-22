package com.zsj.roombooking.mapper;

import com.zsj.roombooking.model.result.RoomSchedule;
import com.zsj.roombooking.model.dto.response.RoomScheduleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoomScheduleMapper {
    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private OccupationMapper occupationMapper;

    public RoomScheduleResponse toResponse(RoomSchedule roomSchedule) {
        return new RoomScheduleResponse(
                roomMapper.toResponse(roomSchedule.room()),
                roomSchedule.occupations().stream().map(occupationMapper::toResponse).toList());
    }
}
