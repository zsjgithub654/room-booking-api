package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<User> searchUsers(String username, Boolean isAdmin, UserStatus status, Pageable pageable);
    User getUser(Long id);
    User addUser(User user);
    User updateUsername(Long id, String username);
    User updatePassword(Long id, String password);
    User addAdminRole(Long id);
    User removeAdminRole(Long id);
    void closeUserAccount(Long id);
}
