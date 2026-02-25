package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.model.RoleType;
import com.zsj.RoomBooking.entity.User;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.zsj.RoomBooking.model.RoleType.ROLE_TYPE_ADMIN;
import static com.zsj.RoomBooking.model.RoleType.ROLE_TYPE_USER;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User addUser(String userName, String password, boolean isAdmin) {
        Set<RoleType> roles = new HashSet<>();
        roles.add(ROLE_TYPE_USER);
        if (isAdmin) {
            roles.add(ROLE_TYPE_ADMIN);
        }
        return userRepository.save(new User(userName, password, roles));
    }
}