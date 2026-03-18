package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.model.UserRequest;
import com.zsj.RoomBooking.model.UserResponse;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return null;
    }

    @Override
    public UserResponse getUser(Long id) {
        return null;
    }

    @Override
    public UserResponse addUser(UserRequest userRequest) {
        return null;
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        return null;
    }

    @Override
    public UserResponse deleteUser(Long id) {
        return null;
    }

}