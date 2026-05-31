package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
public class ClosureRepositoryTest {
    @Autowired
    private ClosureRepository closureRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Room room;

    /* DataJpaTest rollback DB after each test */
    @BeforeEach
    public void setup() {
        room = roomRepository.save(new Room("101", 12, "building A", null, null));
    }

    @Test
    void findByRoomIdHasResultTest() {
        Closure closure = new Closure(room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        closureRepository.save(closure);
        List<Closure> retrievedClosures = closureRepository.findByRoomId(room.getId());
        assertThat(retrievedClosures).hasSize(1);
        assertThat(retrievedClosures.get(0))
                .usingRecursiveComparison()
                .isEqualTo(closure);
    }

    @Test
    void findByRoomIdNoResultMatchRoomTest() {
        Closure closure = new Closure(room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        closureRepository.save(closure);
        List<Closure> retrievedClosures = closureRepository.findByRoomId(room.getId() + 1);
        assertThat(retrievedClosures).hasSize(0);
    }


    @Test
    void findByRoomIdAndOverlappingHasResultTest() {
        List<Closure> closures = List.of(
                /* startTime < lowerBound, lowerBound < end Time < upperBound */
                new Closure(room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 5, 1, 15, 30, 0, 0)),
                /* lowerBound < end Time < upperBound, lowerBound < endTime < upperBound */
                new Closure(room,
                        LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0)),
                /* lowerBound < startTime < upperBound, end Time > upperBound */
                new Closure(room,
                        LocalDateTime.of(2026, 6, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0)),
                /* startTime < lowerBound, end Time > upperBound */
                new Closure(room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0))
        );
        closureRepository.saveAll(closures);
        List<Closure> result = closureRepository.findByRoomIdAndOverlapping(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(4);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(closures);
    }


    @Test
    void findByRoomIdAndOverlappingNoResultMatchRoomTest() {
        Closure closure = new Closure(room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        closureRepository.save(closure);

        List<Closure> result = closureRepository.findByRoomIdAndOverlapping(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingNoResultMatchTimeTest() {
        Closure closure = new Closure(room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        closureRepository.save(closure);

        List<Closure> result = closureRepository.findByRoomIdAndOverlapping(room.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 8, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingOrAdjacentHasResultTest() {
        List<Closure> closures = List.of(
                new Closure(room,
                        LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                        LocalDateTime.of(2026, 5, 1, 15, 30, 0, 0)),
                new Closure(room,
                        LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0)),
                new Closure(room,
                        LocalDateTime.of(2026, 6, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0)),
                new Closure(room,
                        LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0))
        );
        closureRepository.saveAll(closures);

        List<Closure> result = closureRepository.findByRoomIdAndOverlappingOrAdjacent(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(4);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(closures);
    }

    @Test
    void deleteByRoomIdTest() {
        List<Closure> closures = List.of(
                new Closure(room,
                        LocalDateTime.of(2026, 5, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 5, 1, 15, 30, 0, 0)),
                new Closure(room,
                        LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0)));
        closureRepository.saveAll(closures);

        closureRepository.deleteByRoomIdAndAfterTime(room.getId(),
                LocalDateTime.of(2026, 6, 1, 0, 0, 0, 0));
        assertThat(closureRepository.findByRoomId(room.getId())).hasSize(1);
    }
}
