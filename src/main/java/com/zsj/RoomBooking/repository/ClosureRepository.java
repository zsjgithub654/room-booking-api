package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.TimeRange;
import com.zsj.RoomBooking.model.entity.Closure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClosureRepository extends JpaRepository<Closure, Long> {
    List<Closure> findByRoomId(Long roomId);

    /* get closures that overlapping with given interval as TimeRange */
    @Query("""
                SELECT new com.zsj.RoomBooking.model.TimeRange(closure.startTime, closure.endTime) FROM Closure closure
                WHERE closure.room.id = :roomId
                  AND closure.startTime < :toTime
                  AND closure.endTime > :fromTime
            """)
    List<TimeRange> getTimeByRoomIdAndOverlapping(
            Long roomId,
            LocalDateTime fromTime,
            LocalDateTime toTime
    );

    /* get closures that overlapping or adjacent with given interval as TimeRange */
    @Query("""
                SELECT closure FROM Closure closure
                WHERE closure.room.id = :roomId
                  AND closure.startTime <= :toTime
                  AND closure.endTime >= :fromTime
            """)
    List<Closure> findByRoomIdAndOverlappingOrAdjacent(
            Long roomId,
            LocalDateTime fromTime,
            LocalDateTime toTime
    );

    @Modifying
    @Query("""
                DELETE FROM Closure closure
                WHERE closure.room.id = :roomId
                  AND closure.startTime > :fromTime
            """)
    void deleteByRoomIdAndAfterTime(Long roomId, LocalDateTime fromTime);
}
