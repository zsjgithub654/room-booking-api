package com.zsj.roombooking.mapper;

import com.zsj.roombooking.model.dto.request.RoomRequest;
import com.zsj.roombooking.model.dto.response.RoomResponse;
import com.zsj.roombooking.model.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {
    public RoomResponse toResponse(Room room) {
        return new RoomResponse(room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getArea(),
                room.getOpenTime(), room.getCloseTime(),
                room.getStatus());
    }

    public Room toEntity(RoomRequest request) {
        return new Room(request.name(),
                request.capacity(),
                request.area(),
                request.openTime(), request.closeTime());
    }
}
