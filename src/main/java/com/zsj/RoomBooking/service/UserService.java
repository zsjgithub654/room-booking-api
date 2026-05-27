package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;

import java.util.List;

public interface UserService {
    List<User> searchUsers(String username, Role role, UserStatus status);
    User getUser(Long id);
    User addUser(User user);
    User updateUsername(Long id, String username);
    User updatePassword(Long id, String password);
    void closeUserAccount(Long id);
}