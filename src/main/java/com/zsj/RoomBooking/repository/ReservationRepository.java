package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRoomId(Long roomId);

    @Query("""
            SELECT reservation
            FROM Reservation reservation
            WHERE (:userId IS NULL OR reservation.user.id = :userId)
              AND (:roomId IS NULL OR reservation.room.id = :roomId)
              AND (:fromTime IS NULL OR reservation.startTime >= :fromTime)
              AND (:toTime IS NULL OR reservation.startTime < :toTime)
              AND (:status IS NULL OR reservation.status = :status)
            """)
    Page<Reservation> findByUserIdAndRoomIdAndStartTimeAndStatus(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("status") ReservationStatus status,
            Pageable pageable
    );

    @Query("""
                SELECT reservation
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.startTime < :toTime
                  AND reservation.endTime > :fromTime
            """)
    List<Reservation> findByRoomIdAndOverlappingAndActive(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            Sort sort
    );

    @Query("""
                SELECT COUNT(reservation) > 0
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.startTime < :toTime
                  AND reservation.endTime > :fromTime
            """)
    boolean existsByRoomIdAndOverlappingAndActive(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Query("""
                SELECT COUNT(reservation) > 0
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.id <> :reservationId
                  AND reservation.startTime < :toTime
                  AND reservation.endTime > :fromTime
            """)
    boolean existsByRoomIdAndOverlappingAndActiveExcludingReservation(
            @Param("roomId") Long roomId,
            @Param("reservationId") Long reservationId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Query("""
                SELECT reservation
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.startTime > :fromTime
            """)
    List<Reservation> findByRoomIdAndStartAfterAndActive(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            Sort sort
    );

    @Query("""
                SELECT reservation
                FROM Reservation reservation
                WHERE reservation.user.id = :userId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.startTime > :fromTime
            """)
    List<Reservation> findByUserIdAndStartAfterAndActive(
            @Param("userId") Long userId,
            @Param("fromTime") LocalDateTime fromTime,
            Sort sort
    );
}
