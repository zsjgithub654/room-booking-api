package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
              AND (:date IS NULL OR cast(reservation.startTime as date) = :date)
              AND (:status IS NULL OR reservation.status = :status)
            """)
    List<Reservation> findByUserIdAndRoomIdAndDateAndStatus(Long userId, Long roomId, LocalDate date, ReservationStatus status);

    @Query("""
                SELECT reservation
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.startTime < :toTime
                  AND reservation.endTime > :fromTime
            """)
    List<Reservation> findByRoomIdAndOverlappingAndActive(Long roomId, LocalDateTime fromTime, LocalDateTime toTime);

    @Query("""
                SELECT COUNT(reservation) > 0
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.startTime < :toTime
                  AND reservation.endTime > :fromTime
            """)
    boolean existsByRoomIdAndOverlappingAndActive(Long roomId, LocalDateTime fromTime, LocalDateTime toTime);

    @Query("""
                SELECT reservation
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.startTime > :fromTime
            """)
    List<Reservation> findByRoomIdAndStartAfterAndActive(Long roomId, LocalDateTime fromTime);

    @Query("""
                SELECT reservation
                FROM Reservation reservation
                WHERE reservation.user.id = :userId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.startTime > :fromTime
            """)
    List<Reservation> findByUserIdAndStartAfterAndActive(Long userId, LocalDateTime fromTime);
}
