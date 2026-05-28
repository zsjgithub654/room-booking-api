package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.TimeRange;
import com.zsj.RoomBooking.model.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRoomId(Long roomId);

    @Query("""
                SELECT new com.zsj.RoomBooking.model.TimeRange(reservation.startTime, reservation.endTime)
                FROM Reservation reservation
                WHERE reservation.room.id = :roomId
                  AND reservation.status = RESERVATION_STATUS_ACTIVE
                  AND reservation.startTime < :toTime
                  AND reservation.endTime > :fromTime
            """)
    List<TimeRange> getTimeByRoomIdAndOverlappingAndActive(Long roomId, LocalDateTime fromTime, LocalDateTime toTime);

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
