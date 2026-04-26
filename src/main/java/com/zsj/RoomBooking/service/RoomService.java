package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.dto.request.SearchRoomRequest;
import com.zsj.RoomBooking.model.dto.request.RoomRequest;
import com.zsj.RoomBooking.model.dto.response.DeleteRoomResponse;
import com.zsj.RoomBooking.model.dto.response.RoomResponse;
import com.zsj.RoomBooking.model.dto.request.SearchAvailabilityRequest;
import com.zsj.RoomBooking.model.dto.response.SearchAvailabilityResponse;

import java.util.List;

public interface RoomService {
    List<RoomResponse> searchRooms(SearchRoomRequest searchRoomRequest);
    List<SearchAvailabilityResponse> searchAvailabilities(SearchAvailabilityRequest searchRoomRequest);
    RoomResponse getRoom(Long id);
    RoomResponse addRoom(RoomRequest roomRequest);
    DeleteRoomResponse deleteRoom(Long id);
    RoomResponse updateRoom(Long id, RoomRequest roomRequest);
}