package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.entity.Room;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private RoomService service;

    @GetMapping
    public List<Room> getRooms() {
        return service.getAllRooms();
    }

    @PostMapping
    public Room addRoom(String name, int capacity, String area) {
        return service.addRoom(name, capacity, area);
    }
}
