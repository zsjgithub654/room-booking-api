package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface UserRepository extends JpaRepository<User, Long> {
    /* select all when arg is null */
    @Query("""
            SELECT user FROM User user
            WHERE (:username IS NULL OR user.username LIKE %:username%)
            AND (:role IS NULL OR :role MEMBER OF user.roles)
            AND (:status IS NULL OR :status = user.status)
            """)
    List<User> findByUsernameAndRoleAndStatus(String username, Role role, UserStatus status);

    /* TODO: make query parameter binding explicit with @Param across the repositories */
    @Lock(PESSIMISTIC_WRITE)
    @Query("SELECT user FROM User user WHERE user.id = :id")
    Optional<User> findByIdWithLock(Long id);

}
