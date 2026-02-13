package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.RoomRequest;
import com.zsj.RoomBooking.model.RoomResponse;
import com.zsj.RoomBooking.model.SearchRoomRequest;
import com.zsj.RoomBooking.model.UpdateRoomRequest;
import com.zsj.RoomBooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<RoomResponse> getAllRooms() {
        return service.getAllRooms();
    }

    @GetMapping
    public List<RoomResponse> searchRooms(@RequestParam SearchRoomRequest searchRoomRequest) {
        return null;
    }

    /* TODO: Non-null type argument is expected for ResponseEntity */
    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(@RequestBody RoomRequest roomRequest) {
        return new ResponseEntity<>(service.addRoom(roomRequest), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public RoomResponse deleteRoom(@PathVariable Long id) {
    return service.deleteRoom(id);
    }

    @PutMapping("/{id}")
    public RoomResponse updateRoom(@PathVariable Long id, @RequestBody UpdateRoomRequest updateRoomRequest)  {
        return null;
    }
}
