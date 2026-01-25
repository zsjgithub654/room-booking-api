package com.zsj.RoomBooking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoomController {
    @Autowired
    private RoomRepository repository;
    @GetMapping("/")
    public List<Room> greeting() {
        repository.save(new Room("101", 6));
        return repository.findAll();
    }
}
