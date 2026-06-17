package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    List<Reservation> findByRoomId(Long roomId);

    @Query("""
                SELECT reservation
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_SCHEDULED
                  AND reservation.startTime < :toTime
                  AND reservation.endTime > :fromTime
            """)
    List<Reservation> findByRoomIdAndOverlappingAndScheduled(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            Sort sort
    );

    @Query("""
                SELECT COUNT(reservation) > 0
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_SCHEDULED
                  AND reservation.startTime < :toTime
                  AND reservation.endTime > :fromTime
            """)
    boolean existsByRoomIdAndOverlappingAndScheduled(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Query("""
                SELECT COUNT(reservation) > 0
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_SCHEDULED
                  AND reservation.id <> :reservationId
                  AND reservation.startTime < :toTime
                  AND reservation.endTime > :fromTime
            """)
    boolean existsByRoomIdAndOverlappingAndScheduledExcludingReservation(
            @Param("roomId") Long roomId,
            @Param("reservationId") Long reservationId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Query("""
                SELECT reservation
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_SCHEDULED
                  AND reservation.startTime > :fromTime
            """)
    List<Reservation> findByRoomIdAndStartAfterAndScheduled(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            Sort sort
    );

    @Query("""
                SELECT reservation
                FROM Reservation reservation
                WHERE reservation.user.id = :userId
                  AND reservation.status = RESERVATION_STATUS_SCHEDULED
                  AND reservation.startTime > :fromTime
            """)
    List<Reservation> findByUserIdAndStartAfterAndScheduled(
            @Param("userId") Long userId,
            @Param("fromTime") LocalDateTime fromTime,
            Sort sort
    );
}
