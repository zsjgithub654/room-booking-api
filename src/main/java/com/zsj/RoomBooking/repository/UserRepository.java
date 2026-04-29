package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
