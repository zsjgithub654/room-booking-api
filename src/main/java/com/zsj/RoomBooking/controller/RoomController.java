package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.dto.request.SearchRoomRequest;
import com.zsj.RoomBooking.model.dto.response.AddClosureResponse;
import com.zsj.RoomBooking.model.dto.response.ClosureResponse;
import com.zsj.RoomBooking.model.dto.request.RoomRequest;
import com.zsj.RoomBooking.model.dto.response.DeleteRoomResponse;
import com.zsj.RoomBooking.model.dto.response.RoomResponse;
import com.zsj.RoomBooking.model.dto.request.SearchAvailabilityRequest;
import com.zsj.RoomBooking.model.dto.request.TimeRangeRequest;
import com.zsj.RoomBooking.model.dto.response.SearchAvailabilityResponse;
import com.zsj.RoomBooking.service.ClosureService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private ClosureService closureService;

    @GetMapping
    public List<RoomResponse> searchRooms(@ModelAttribute SearchRoomRequest request) {
        return roomService.searchRooms(request);
    }

    @GetMapping("/availabilities")
    public List<SearchAvailabilityResponse> searchAvailabilities(@ModelAttribute SearchAvailabilityRequest request) {
        return roomService.searchAvailabilities(request);
    }

    @GetMapping("/{roomId}")
    public RoomResponse getRoom(@PathVariable Long roomId) {
        return roomService.getRoom(roomId);
    }

    /* TODO: Non-null type argument is expected for ResponseEntity */
    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(@RequestBody RoomRequest roomRequest) {
        return new ResponseEntity<>(roomService.addRoom(roomRequest), HttpStatus.CREATED);
    }

    @DeleteMapping("/{roomId}")
    public DeleteRoomResponse deleteRoom(@PathVariable Long roomId) {
        return roomService.deleteRoom(roomId);
    }

    @PutMapping("/{roomId}")
    public RoomResponse updateRoom(@PathVariable Long roomId, @RequestBody RoomRequest roomRequest)  {
        return roomService.updateRoom(roomId, roomRequest);
    }

    /* closure */
    /* TODO: do we need an update? */
    @GetMapping("/{roomId}/closures")
    public List<ClosureResponse> getClosures(@PathVariable Long roomId) {
        return closureService.getClosuresForRoom(roomId);
    }

    @PostMapping("/{roomId}/closures")
    public ResponseEntity<AddClosureResponse> addClosure(@PathVariable Long roomId, @RequestParam Long userId, @RequestBody TimeRangeRequest timeRangeRequest) {
        return new ResponseEntity<>(closureService.addClosure(roomId, userId, timeRangeRequest), HttpStatus.CREATED);
    }
}
