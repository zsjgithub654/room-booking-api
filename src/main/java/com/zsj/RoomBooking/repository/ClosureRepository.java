package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.TimeRange;
import com.zsj.RoomBooking.model.entity.Closure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClosureRepository extends JpaRepository<Closure, Long> {
    List<Closure> findByRoomId(Long roomId);

    @Query("""
                SELECT new com.zsj.RoomBooking.model.TimeRange(closure.startTime, closure.endTime) FROM Closure closure
                WHERE closure.room.id = :roomId
                  AND closure.startTime < :endTime
                  AND closure.endTime > :startTime
            """)
    List<TimeRange> getTimeByRoomIdAndInterval(
            Long roomId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    void deleteByRoomId(Long roomId);
}