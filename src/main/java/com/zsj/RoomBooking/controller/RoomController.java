package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.AddClosureResponse;
import com.zsj.RoomBooking.model.ClosureResponse;
import com.zsj.RoomBooking.model.RoomRequest;
import com.zsj.RoomBooking.model.RoomResponse;
import com.zsj.RoomBooking.model.SearchRoomRequest;
import com.zsj.RoomBooking.model.TimeRangeRequest;
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
    private RoomService service;

    @GetMapping
    public List<RoomResponse> searchRooms(@ModelAttribute SearchRoomRequest searchRoomRequest) {
        return service.searchRooms(searchRoomRequest);
    }

    @GetMapping("/{roomId}")
    public RoomResponse getRoom(@PathVariable Long roomId) {
        return service.getRoom(roomId);
    }

    /* TODO: Non-null type argument is expected for ResponseEntity */
    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(@RequestBody RoomRequest roomRequest) {
        return new ResponseEntity<>(service.addRoom(roomRequest), HttpStatus.CREATED);
    }

    @DeleteMapping("/{roomId}")
    public RoomResponse deleteRoom(@PathVariable Long roomId) {
        return service.deleteRoom(roomId);
    }

    @PutMapping("/{roomId}")
    public RoomResponse updateRoom(@PathVariable Long roomId, @RequestBody RoomRequest roomRequest)  {
        return service.updateRoom(roomId, roomRequest);
    }

    /* closure */
    @GetMapping("/{roomId}/closures")
    public List<ClosureResponse> getClosures(@PathVariable Long roomId) {
        return service.getClosures(roomId);
    }

    @PostMapping("/{roomId}/closures")
    public ResponseEntity<AddClosureResponse> addClosure(@PathVariable Long roomId, @RequestParam Long userId, @RequestBody TimeRangeRequest timeRangeRequest) {
        return new ResponseEntity<>(service.addClosure(roomId, userId, timeRangeRequest), HttpStatus.CREATED);
    }

    @DeleteMapping("/{roomId}/closures/{closureId}")
    public ClosureResponse deleteClosure(@PathVariable Long roomId, @PathVariable Long closureId) {
        return service.deleteClosure(roomId, closureId);
    }
}
