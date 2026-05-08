package com.zsj.RoomBooking.mapper;

import com.zsj.RoomBooking.model.dto.request.RoomRequest;
import com.zsj.RoomBooking.model.dto.response.RoomResponse;
import com.zsj.RoomBooking.model.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {
    public RoomResponse toResponse(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity(), room.getArea());
    }

    public Room toEntity(RoomRequest request) {
        return new Room(request.name(), request.capacity(), request.area());
    }
}
