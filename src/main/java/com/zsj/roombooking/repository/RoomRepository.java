package com.zsj.roombooking.repository;

import com.zsj.roombooking.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    /* to handle concurrency in reservation creation, used in: reservation creation/update, closure creation */
    @Lock(PESSIMISTIC_WRITE)
    @Query("SELECT room FROM Room room WHERE room.id = :id")
    Optional<Room> findByIdWithLock(@Param("id") Long id);
}

