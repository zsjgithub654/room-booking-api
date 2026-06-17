package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    boolean existsByRolesContainsAndStatus(Role role, UserStatus status);

    boolean existsByRolesContainsAndStatusAndIdNot(Role role, UserStatus status, Long id);

    @Lock(PESSIMISTIC_WRITE)
    @Query("SELECT user FROM User user WHERE user.id = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);

}
