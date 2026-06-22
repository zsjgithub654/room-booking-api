package com.zsj.roombooking.mapper;

import com.zsj.roombooking.model.dto.request.UserRequest;
import com.zsj.roombooking.model.dto.response.UserResponse;
import com.zsj.roombooking.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRoles(), user.getStatus());
    }

    public User toEntity(UserRequest request) {
        return new User(request.username(), request.password());
    }

}
