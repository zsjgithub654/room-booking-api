package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.UserRequest;
import com.zsj.RoomBooking.model.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUser(Long id);
    UserResponse addUser(UserRequest userRequest);
    UserResponse updateUser(Long id, UserRequest userRequest);
    UserResponse deleteUser(Long id);
}