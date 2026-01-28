package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.RoleType;
import com.zsj.RoomBooking.entity.User;
import com.zsj.RoomBooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.zsj.RoomBooking.RoleType.ROLE_TYPE_ADMIN;
import static com.zsj.RoomBooking.RoleType.ROLE_TYPE_NORMAL;

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private UserRepository repository;

    @GetMapping
    public List<User> getUsers() {
        return repository.findAll();
    }

    @PostMapping
    public void addUser(String userName, String password, boolean isAdmin) {
        Set<RoleType> roles = new HashSet<>();
        roles.add(ROLE_TYPE_NORMAL);
        if (isAdmin) {
            roles.add(ROLE_TYPE_ADMIN);
        }
        repository.save(new User(userName, password, roles));
    }
}
