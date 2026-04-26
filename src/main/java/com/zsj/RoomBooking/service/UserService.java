package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.dto.request.UserRequest;
import com.zsj.RoomBooking.model.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> searchUsers();
    UserResponse getUser(Long id);
    UserResponse addUser(UserRequest userRequest);
    UserResponse updateUser(Long id, UserRequest userRequest);
    UserResponse deleteUser(Long id);
}