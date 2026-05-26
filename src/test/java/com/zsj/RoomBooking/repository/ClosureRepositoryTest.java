package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.TimeRange;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
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

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Room room;

    @BeforeEach
    /* DateJpaTest rollback DB after each test */
    public void setup() {
        room = roomRepository.save(new Room("101", 12, "building A"));
        user = userRepository.save(new User("user1", ""));
    }

    @Test
    void findByRoomIdHasResultTest() {
        Closure closure = new Closure(user, room,
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
        Closure closure = new Closure(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        closureRepository.save(closure);
        List<Closure> retrievedClosures = closureRepository.findByRoomId(room.getId() + 1);
        assertThat(retrievedClosures).hasSize(0);
    }


    @Test
    void getTimeByRoomIdAndIntervalHasResultTest() {
        List<Closure> closures = List.of(
                /* startTime < lowerBound, lowerBound < end Time < upperBound */
                new Closure(user, room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 5, 1, 15, 30, 0, 0)),
                /* lowerBound < end Time < upperBound, lowerBound < endTime < upperBound */
                new Closure(user, room,
                        LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0)),
                /* lowerBound < end Time < upperBound, end Time > upperBound */
                new Closure(user, room,
                        LocalDateTime.of(2026, 6, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0)),
                /* startTime < lowerBound, end Time > upperBound */
                new Closure(user, room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0))
        );
        closureRepository.saveAll(closures);
        List<TimeRange> timeRanges = closureRepository.getTimeByRoomIdAndInterval(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(timeRanges).hasSize(4);
        assertThat(timeRanges)
                .usingRecursiveComparison()
                .isEqualTo(closures);
    }

    @Test
    void getTimeByRoomIdAndIntervalNoResultMatchRoomTest() {
        Closure closure = new Closure(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        closureRepository.save(closure);

        List<TimeRange> timeRanges = closureRepository.getTimeByRoomIdAndInterval(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(timeRanges).hasSize(0);
    }

    @Test
    void getTimeByRoomIdAndIntervalNoResultMatchTimeTest() {
        Closure closure = new Closure(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        closureRepository.save(closure);

        List<TimeRange> timeRanges = closureRepository.getTimeByRoomIdAndInterval(room.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 8, 1, 0, 0, 0, 0));
        assertThat(timeRanges).hasSize(0);
    }

    @Test
    void deleteByRoomIdTest() {
        Closure closure = new Closure(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        closureRepository.save(closure);
        assertThat(closureRepository.findByRoomId(room.getId())).hasSize(1);

        closureRepository.deleteByRoomId(room.getId());
        assertThat(closureRepository.findByRoomId(room.getId())).hasSize(0);
    }
}
