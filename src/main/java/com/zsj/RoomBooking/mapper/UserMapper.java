package com.zsj.RoomBooking.mapper;

import com.zsj.RoomBooking.model.dto.request.UserRequest;
import com.zsj.RoomBooking.model.dto.response.UserResponse;
import com.zsj.RoomBooking.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRoles());
    }

    public User toEntity(UserRequest request) {
        return new User(request.username(), request.password());
    }

}
