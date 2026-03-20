package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.AddClosureResponse;
import com.zsj.RoomBooking.model.ClosureRequest;
import com.zsj.RoomBooking.model.ClosureResponse;
import com.zsj.RoomBooking.model.RoomRequest;
import com.zsj.RoomBooking.model.RoomResponse;
import com.zsj.RoomBooking.model.SearchRoomRequest;

import java.util.List;

public interface RoomService {
    List<RoomResponse> searchRooms(SearchRoomRequest searchRoomRequest);
    RoomResponse getRoom(Long id);
    RoomResponse addRoom(RoomRequest roomRequest);
    RoomResponse deleteRoom(Long id);
    RoomResponse updateRoom(Long id, RoomRequest roomRequest);
    List<ClosureResponse> getClosures(Long roomId);
    AddClosureResponse addClosure(Long roomId, ClosureRequest closureRequest);
    ClosureResponse deleteClosure(Long roomId, Long closureId);
}