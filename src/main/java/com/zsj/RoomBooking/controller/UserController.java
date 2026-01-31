package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.entity.User;
import com.zsj.RoomBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private UserService service;

    @GetMapping
    public List<User> getUsers() {
        return service.getAllUsers();
    }

    @PostMapping
    public User addUser(String userName, String password, boolean isAdmin) {
        return service.addUser(userName, password, isAdmin);
    }
}
