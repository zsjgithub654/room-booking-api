package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Sort;

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
    private List<Closure> closures;

    /* DataJpaTest rollback DB after each test */
    @BeforeEach
    public void setup() {
        room = roomRepository.save(new Room("101", 12, "building A", null, null));
        closures = closureRepository.saveAll(List.of(
                new Closure(room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 5, 1, 15, 30, 0, 0)),
                new Closure(room,
                        LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0)),
                new Closure(room,
                        LocalDateTime.of(2026, 6, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0)),
                new Closure(room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0))
        ));
    }

    @Test
    void findByRoomIdHasResultTest() {
        List<Closure> retrievedClosures = closureRepository.findByRoomId(room.getId(), Sort.by(
                Sort.Order.asc("startTime"),
                Sort.Order.asc("endTime"),
                Sort.Order.asc("id")));
        assertThat(retrievedClosures).hasSize(4);
        assertThat(retrievedClosures)
                .usingRecursiveComparison()
                .isEqualTo(List.of(closures.get(0), closures.get(3), closures.get(1), closures.get(2)));
    }

    @Test
    void findByRoomIdNoResultMatchRoomTest() {
        List<Closure> retrievedClosures = closureRepository.findByRoomId(room.getId() + 1, Sort.unsorted());
        assertThat(retrievedClosures).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingHasResultTest() {
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
        List<Closure> result = closureRepository.findByRoomIdAndOverlapping(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingNoResultMatchTimeTest() {
        List<Closure> result = closureRepository.findByRoomIdAndOverlapping(room.getId(),
                LocalDateTime.of(2026, 8, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(0);
    }

    @Test
    void existsByRoomIdAndOverlappingExistsTest() {
        assertThat(closureRepository.existsByRoomIdAndOverlapping(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0))
        ).isTrue();
    }

    @Test
    void existsByRoomIdAndOverlappingNoResultMatchRoomTest() {
        assertThat(closureRepository.existsByRoomIdAndOverlapping(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0))
        ).isFalse();
    }

    @Test
    void existsByRoomIdAndOverlappingNoResultMatchTimeTest() {
        assertThat(closureRepository.existsByRoomIdAndOverlapping(room.getId(),
                LocalDateTime.of(2026, 8, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0, 0, 0))
        ).isFalse();
    }

    @Test
    void findByRoomIdAndOverlappingOrAdjacentHasResultTest() {
        List<Closure> result = closureRepository.findByRoomIdAndOverlappingOrAdjacent(room.getId(),
                LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 30, 14, 30, 0, 0));
        assertThat(result).hasSize(4);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(closures);
    }

    @Test
    void deleteByRoomIdAndStartAfterTest() {
        closureRepository.deleteByRoomIdAndStartAfter(room.getId(),
                LocalDateTime.of(2026, 6, 1, 0, 0, 0, 0));
        assertThat(closureRepository.findByRoomId(room.getId(), Sort.unsorted())).hasSize(2);
    }
}
