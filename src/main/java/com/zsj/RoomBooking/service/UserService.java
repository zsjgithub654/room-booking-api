package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.entity.User;

import java.util.List;

public interface UserService {
    List<User> searchUsers();
    User getUser(Long id);
    User addUser(User user);
    User updateUser(Long id, String username, String password);
    void deleteUser(Long id);
}