package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByRolesContainsAndStatus(Role role, UserStatus status);

    /* select all when arg is null */
    @Query("""
            SELECT user FROM User user
            WHERE (:username IS NULL OR user.username LIKE %:username%)
            AND (:role IS NULL OR :role MEMBER OF user.roles)
            AND (:status IS NULL OR :status = user.status)
            """)
    Page<User> findByUsernameAndRoleAndStatus(
            @Param("username") String username,
            @Param("role") Role role,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    @Lock(PESSIMISTIC_WRITE)
    @Query("SELECT user FROM User user WHERE user.id = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);

}
