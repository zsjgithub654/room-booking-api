package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.RoomScheduleMapper;
import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.mapper.RoomMapper;
import com.zsj.RoomBooking.model.dto.request.SearchRoomRequest;
import com.zsj.RoomBooking.model.dto.request.RoomRequest;
import com.zsj.RoomBooking.model.dto.response.DeleteRoomResponse;
import com.zsj.RoomBooking.model.dto.response.RoomResponse;
import com.zsj.RoomBooking.model.dto.request.SearchAvailabilityRequest;
import com.zsj.RoomBooking.model.dto.response.RoomScheduleResponse;
import com.zsj.RoomBooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private RoomScheduleMapper roomScheduleMapper;

    @GetMapping
    public List<RoomResponse> searchRooms(@ModelAttribute SearchRoomRequest request) {
        return roomService.searchRooms(
                request.name(),
                request.minCapacity(), request.maxCapacity(),
                request.area()
        ).stream().map(roomMapper::toResponse).toList();
    }

    /* TODO: add constraint on search time range length, or page the result */
    @GetMapping("/availabilities")
    public List<RoomScheduleResponse> searchAvailabilities(@ModelAttribute SearchAvailabilityRequest request) {
        return roomService.searchAvailabilities(
                request.name(),
                request.minCapacity(), request.maxCapacity(),
                request.area(),
                request.startTime(), request.endTime()
        ).stream().map(roomScheduleMapper::toResponse).toList();
    }

    @GetMapping("/{roomId}")
    /* TODO: rename */
    public RoomResponse getRoom(@PathVariable Long roomId) {
        return roomMapper.toResponse(roomService.getRoom(roomId));
    }

    /* TODO: Non-null type argument is expected for ResponseEntity */
    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(@RequestBody RoomRequest request) {
        return new ResponseEntity<>(roomMapper.toResponse(
                roomService.addRoom(roomMapper.toEntity(request))),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{roomId}")
    public DeleteRoomResponse deleteRoom(@PathVariable Long roomId) {
        return new DeleteRoomResponse(
                roomId,
                roomService.deleteRoom(roomId).stream().map(reservationMapper::toResponse).toList()
        );
    }

    @PutMapping("/{roomId}")
    public RoomResponse updateRoom(@PathVariable Long roomId, @RequestBody RoomRequest request) {
        return roomMapper.toResponse(
                roomService.updateRoom(roomId,
                        request.name(),
                        request.capacity(),
                        request.area(),
                        request.openTime(),
                        request.closeTime())
        );
    }
}
