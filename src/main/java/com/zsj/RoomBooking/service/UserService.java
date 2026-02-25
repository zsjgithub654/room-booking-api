package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.entity.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User addUser(String userName, String password, boolean isAdmin);
}