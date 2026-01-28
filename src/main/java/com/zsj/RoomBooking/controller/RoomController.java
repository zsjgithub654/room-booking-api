package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.entity.Room;
import com.zsj.RoomBooking.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    // TODO: should it be singleton?
    @Autowired
    private RoomRepository repository;

    @GetMapping
    public List<Room> getRooms() {
        return repository.findAll();
    }

    @PostMapping
    public void addRoom(String displayName, int capacity, String area) {
        repository.save(new Room(displayName, capacity, area));
    }
}
