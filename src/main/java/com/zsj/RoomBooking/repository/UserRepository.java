package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
