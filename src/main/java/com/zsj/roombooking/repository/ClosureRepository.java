package com.zsj.roombooking.repository;

import com.zsj.roombooking.model.entity.Closure;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClosureRepository extends JpaRepository<Closure, Long> {
    List<Closure> findByRoomId(Long roomId, Sort sort);

    /* get closures that overlapping with given interval */
    @Query("""
                SELECT closure FROM Closure closure
                WHERE closure.room.id = :roomId
                  AND closure.startTime < :toTime
                  AND closure.endTime > :fromTime
            """)
    List<Closure> findByRoomIdAndOverlapping(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Query("""
                SELECT COUNT(closure) > 0 FROM Closure closure
                WHERE closure.room.id = :roomId
                  AND closure.startTime < :toTime
                  AND closure.endTime > :fromTime
            """)
    boolean existsByRoomIdAndOverlapping(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    /* get closures that overlapping or adjacent with given interval */
    @Query("""
                SELECT closure FROM Closure closure
                WHERE closure.room.id = :roomId
                  AND closure.startTime <= :toTime
                  AND closure.endTime >= :fromTime
            """)
    List<Closure> findByRoomIdAndOverlappingOrAdjacent(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );

    @Modifying
    @Query("""
                DELETE FROM Closure closure
                WHERE closure.room.id = :roomId
                  AND closure.startTime > :fromTime
            """)
    void deleteByRoomIdAndStartAfter(
            @Param("roomId") Long roomId,
            @Param("fromTime") LocalDateTime fromTime
    );
}
