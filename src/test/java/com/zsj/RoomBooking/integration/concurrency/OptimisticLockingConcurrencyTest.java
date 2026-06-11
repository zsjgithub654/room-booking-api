package com.zsj.RoomBooking.integration.concurrency;

import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static com.zsj.RoomBooking.integration.concurrency.ConcurrencyTestUtils.assertOptimisticLockingFailure;

/**
 * This test suite tests optimistic locking.
 * This suite is mainly to ensure that @Version has been correctly added to entities and that optimistic locking works.
 * The races are kept simple as concurrency between services has already been tested in {@link ServiceConcurrencyTest},
 */

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.jpa.hibernate.ddl-auto=create-drop"}
)
class OptimisticLockingConcurrencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ClosureRepository closureRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-trixie");

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setup() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        reservationRepository.deleteAll();
        closureRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void userOptimisticLockingTest() {
        User user = userRepository.save(new User("testUser", "password"));
        Long userId = user.getId();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            /* transaction */
            Throwable failure = catchThrowable(() -> transactionTemplate.execute(status -> {
                User version1 = userRepository.findById(userId).orElseThrow();
                /* another transaction that read the same version but commit earlier */
                Future<?> winner = executor.submit(() -> transactionTemplate.execute(innerStatus -> {
                    User version2 = userRepository.findById(userId).orElseThrow();
                    version2.setUsername("userName2");
                    return null;
                }));
                /* wait for winner to finish */
                try {
                    assertThat(winner.get(10, TimeUnit.SECONDS)).isNull();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                /* write the stale entity */
                version1.setUsername("userName1");
                /* required, trigger update */
                userRepository.flush();
                return null;
            }));
            /* write on stale entity should fail */
            assertThat(failure).isNotNull();
            assertOptimisticLockingFailure(failure);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void roomOptimisticLockingTest() {
        Room room = roomRepository.save(new Room("101", 10, "Area A", null, null));
        Long roomId = room.getId();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Throwable failure = catchThrowable(() -> transactionTemplate.execute(status -> {
                Room version1 = roomRepository.findById(roomId).orElseThrow();
                Future<?> winner = executor.submit(() -> transactionTemplate.execute(innerStatus -> {
                    Room version2 = roomRepository.findById(roomId).orElseThrow();
                    version2.setName("roomName2");
                    return null;
                }));
                try {
                    assertThat(winner.get(10, TimeUnit.SECONDS)).isNull();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                version1.setName("roomName1");
                roomRepository.flush();
                return null;
            }));

            assertThat(failure).isNotNull();
            assertOptimisticLockingFailure(failure);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void reservationOptimisticLockingTest() {
        User user = userRepository.save(new User("user", "pass"));
        Room room = roomRepository.save(new Room("101", 10, "Area A", null, null));
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 1, 10, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 1, 11, 0, 0, 0);
        Reservation reservation = reservationRepository.save(new Reservation(user, room, startTime, endTime));
        Long reservationId = reservation.getId();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Throwable failure = catchThrowable(() -> transactionTemplate.execute(status -> {
                Reservation version1 = reservationRepository.findById(reservationId).orElseThrow();
                Future<?> winner = executor.submit(() -> transactionTemplate.execute(innerStatus -> {
                    Reservation version2 = reservationRepository.findById(reservationId).orElseThrow();
                    version2.setTime(startTime.plusMinutes(10), endTime.plusMinutes(10));
                    return null;
                }));
                try {
                    assertThat(winner.get(10, TimeUnit.SECONDS)).isNull();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                version1.setTime(startTime.plusMinutes(20), endTime.plusMinutes(20));
                reservationRepository.flush();
                return null;
            }));

            assertThat(failure).isNotNull();
            assertOptimisticLockingFailure(failure);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void closureOptimisticLockingTest() {
        Room room = roomRepository.save(new Room("101", 10, "Area A", null, null));
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 1, 10, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 1, 12, 0, 0, 0);
        Closure closure = closureRepository.save(new Closure(room, startTime, endTime));
        Long closureId = closure.getId();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Throwable failure = catchThrowable(() -> transactionTemplate.execute(status -> {
                Closure version1 = closureRepository.findById(closureId).orElseThrow();
                Future<?> winner = executor.submit(() -> transactionTemplate.execute(innerStatus -> {
                    Closure version2 = closureRepository.findById(closureId).orElseThrow();
                    closureRepository.delete(version2);
                    return null;
                }));
                try {
                    assertThat(winner.get(10, TimeUnit.SECONDS)).isNull();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                closureRepository.delete(version1);
                closureRepository.flush();
                return null;
            }));

            assertThat(failure).isNotNull();
            assertOptimisticLockingFailure(failure);
        } finally {
            executor.shutdownNow();
        }
    }
}
