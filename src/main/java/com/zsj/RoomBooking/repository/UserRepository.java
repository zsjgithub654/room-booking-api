package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    /* select all when arg is null */
    @Query("""
            SELECT user FROM User user
            WHERE (:username IS NULL OR user.username LIKE %:username%)
            AND (:role IS NULL OR :role MEMBER OF user.roles)
            """)
    List<User> findByUsernameAndRole(String username, Role role);
}
