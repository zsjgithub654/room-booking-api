package com.zsj.RoomBooking.integration.concurrency;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.RoomStatus;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.ClosureService;
import com.zsj.RoomBooking.service.ReservationService;
import com.zsj.RoomBooking.service.RoomService;
import com.zsj.RoomBooking.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static com.zsj.RoomBooking.integration.concurrency.ConcurrencyTestUtils.assertOptimisticLockingFailure;

/**
 * This suite tests concurrent operations on the service layer.
 * The tests cannot guarantee the race condition will occur, and the winner in the race is not fixed. The focus is to
 * validate the final state.
 */
@Testcontainers
@SpringBootTest(
        /* mock web layer to keep servlet/security beans */
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        /* create DB schema on start and drop on end */
        properties = {"spring.jpa.hibernate.ddl-auto=create-drop"}
)
class ServiceConcurrencyTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ClosureService closureService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClosureRepository closureRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-trixie");

    private Room room;
    private User user1;
    private User user2;

    /* to pass lambda as parameter */
    @FunctionalInterface
    private interface ConcurrentAction {
        void run() throws Exception;
    }

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        closureRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        room = roomRepository.save(new Room("101", 12, "Building A", null, null));
        user1 = userRepository.save(new User("user1", ""));
        user2 = userRepository.save(new User("user2", ""));
    }

    @Test
    /* both with Room pessimistic lock */
    void addReservationsConcurrentlyOnlyOneSucceedsTest() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2026, 6, 1, 10, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 6, 1, 11, 0, 0, 0);

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        runConcurrentActions(failures,
                () -> reservationService.addReservation(user1.getId(), room.getId(), startTime, endTime),
                () -> reservationService.addReservation(user2.getId(), room.getId(), startTime, endTime)
        );

        /* exactly one reservation succeeded, the other get an exception. */
        assertThat(failures).hasSize(1);
        assertThat(failures.peek())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Room is reserved in selected time.");
        /* exact one reservation has the desired room and time */
        List<Reservation> reservations = reservationRepository.findByRoomId(room.getId());
        assertThat(reservations).hasSize(1);
        Reservation reservation = reservations.get(0);
        assertThat(reservation.getStartTime()).isEqualTo(startTime);
        assertThat(reservation.getEndTime()).isEqualTo(endTime);
    }

    @Test
    /* both with Room pessimistic lock */
    void addReservationConcurrentWithUpdateReservationOnlyOneSucceedsTest() throws Exception {
        Reservation existingReservation = reservationRepository.save(
                new Reservation(user1, room,
                        LocalDateTime.of(2026, 6, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 11, 0, 0, 0))
        );
        /* new reservation */
        LocalDateTime targetStartTime = LocalDateTime.of(2026, 6, 1, 12, 0, 0, 0);
        LocalDateTime targetEndTime = LocalDateTime.of(2026, 6, 1, 13, 0, 0, 0);

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        runConcurrentActions(failures,
                () -> reservationService.addReservation(user2.getId(), room.getId(), targetStartTime, targetEndTime),
                () -> reservationService.updateReservationTime(existingReservation.getId(), targetStartTime, targetEndTime)
        );

        /* Either the update wins and the add is rejected, or the add wins and the update is rejected. */
        assertThat(failures).hasSize(1);
        assertThat(failures.peek())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Room is reserved in selected time.");
        /* exact one reservation has the desired room and time */
        List<Reservation> reservations = reservationRepository.findByRoomId(room.getId());
        assertThat(reservations).filteredOn(reservation ->
                        reservation.getStartTime().equals(targetStartTime)
                                && reservation.getEndTime().equals(targetEndTime))
                .hasSize(1);
    }

    @Test
    /* both with Room pessimistic lock */
    void addReservationConcurrentWithAddClosureKeepsNoActiveReservationTest() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2300, 6, 1, 10, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 6, 1, 11, 0, 0, 0);

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        runConcurrentActions(failures,
                () -> reservationService.addReservation(user1.getId(), room.getId(), startTime, endTime),
                () -> closureService.addClosure(room.getId(), startTime, endTime)
        );

        /* closure should be added either way */
        List<Closure> closures = closureRepository.findByRoomId(room.getId());
        assertThat(closures).hasSize(1);
        assertThat(closures.get(0).getStartTime()).isEqualTo(startTime);
        assertThat(closures.get(0).getEndTime()).isEqualTo(endTime);

        assertThat(failures).hasSizeLessThanOrEqualTo(1);
        if (failures.isEmpty()) {
            /* Reservation commit first, then closed by closure later, both succeed. */
            List<Reservation> reservations = reservationRepository.findByRoomId(room.getId()).stream()
                    .filter(res -> res.getStartTime().equals(startTime) && res.getEndTime().equals(endTime))
                    .toList();
            assertThat(reservations).hasSize(1);
            assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
        } else {
            /* Closure commit first, reservation failed. */
            assertThat(failures).hasSize(1);
            assertThat(failures.peek())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Room is in closure during selected time.");
            assertThat(reservationRepository.findByRoomId(room.getId())).isEmpty();
        }
    }

    @Test
    /* both with Room pessimistic lock */
    void addReservationConcurrentWithDeleteRoomKeepsNoActiveReservationTest() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2300, 6, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 6, 1, 13, 0, 0, 0);

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        runConcurrentActions(failures,
                () -> reservationService.addReservation(user1.getId(), room.getId(), startTime, endTime),
                () -> roomService.deleteRoom(room.getId())
        );

        /* The room should end up deleted either way. */
        assertThat(roomRepository.findById(room.getId()))
                .isPresent()
                .get()
                .extracting(Room::getStatus)
                .isEqualTo(RoomStatus.ROOM_STATUS_DELETED);

        assertThat(failures).hasSizeLessThanOrEqualTo(1);
        if (failures.isEmpty()) {
            /* Reservation commit first, then closed later, both succeed. */
            List<Reservation> reservations = reservationRepository.findByRoomId(room.getId()).stream()
                    .filter(res -> res.getStartTime().equals(startTime) && res.getEndTime().equals(endTime))
                    .toList();
            assertThat(reservations).hasSize(1);
            assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
        } else {
            /* Close commit first, reservation failed. */
            assertThat(failures).hasSize(1);
            assertThat(failures.peek())
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Room not found.");
            assertThat(reservationRepository.findByRoomId(room.getId())).isEmpty();
        }
    }

    @Test
    /* both with User pessimistic lock */
    void addReservationConcurrentWithDeleteUserKeepsNoActiveReservationTest() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2300, 6, 1, 14, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 6, 1, 15, 0, 0, 0);
        Long userId = user1.getId();

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        runConcurrentActions(failures,
                () -> reservationService.addReservation(userId, room.getId(), startTime, endTime),
                () -> userService.closeUserAccount(userId)
        );

        /* The account should end up closed and scrubbed either way. */
        User currentUser = userRepository.findById(userId).orElseThrow();
        assertThat(currentUser)
                .extracting(User::getStatus, User::getUsername, User::getPassword)
                .containsExactly(UserStatus.USER_STATUS_CLOSED, null, null);

        assertThat(failures).hasSizeLessThanOrEqualTo(1);
        if (failures.isEmpty()) {
            /* Reservation commit first, then closed later, both succeed. */
            List<Reservation> reservations = reservationRepository.findByRoomId(room.getId()).stream()
                    .filter(res -> res.getUser().getId().equals(userId))
                    .filter(res -> res.getStartTime().equals(startTime))
                    .filter(res -> res.getEndTime().equals(endTime))
                    .toList();
            assertThat(reservations).hasSize(1);
            assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
        } else {
            /* Close commit first, reservation failed. */
            assertThat(failures).hasSize(1);
            Throwable failure = failures.peek();
            assertThat(failure)
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found.");
            assertThat(reservationRepository.findByRoomId(room.getId())).isEmpty();
        }
    }

    @Test
    /* both with @Version */
    void updateReservationConcurrentWithDeleteReservationTest() throws Exception {
        Reservation reservation = reservationRepository.save(
                new Reservation(user1, room,
                        LocalDateTime.of(2026, 6, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 11, 0, 0, 0))
        );
        /* new time to update */
        LocalDateTime newStartTime = LocalDateTime.of(2026, 6, 1, 12, 0, 0, 0);
        LocalDateTime newEndTime = LocalDateTime.of(2026, 6, 1, 13, 0, 0, 0);

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        runConcurrentActions(failures,
                () -> reservationService.updateReservationTime(reservation.getId(),
                        newStartTime,
                        newEndTime),
                () -> reservationService.deleteReservation(reservation.getId())
        );

        /* unless update and delete run in serial, one operation will fail */
        assertThat(failures).hasSizeLessThanOrEqualTo(1);
        if (!failures.isEmpty()) {
            Throwable failure = failures.peek();
            if (failure instanceof ResourceNotFoundException) {
                /* delete read after update commit, delete simply cannot find the target, no conflict */
                assertThat(failure).hasMessage("Reservation not found.");
            } else {
                /* write conflict on @Version, one fail with ObjectOptimisticLockingFailureException */
                assertOptimisticLockingFailure(failure);
            }
        }

        /* delete, update: @Version, either transaction can win */
        Reservation currentReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
        if (currentReservation.getStatus() == ReservationStatus.RESERVATION_STATUS_CANCELED) {
            /* delete wins first, leaving the reservation canceled */
            assertThat(currentReservation.getStartTime()).isEqualTo(reservation.getStartTime());
            assertThat(currentReservation.getEndTime()).isEqualTo(reservation.getEndTime());
        } else {
            /* update wins first, leaving the reservation active with the new time */
            assertThat(currentReservation.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
            assertThat(currentReservation.getStartTime()).isEqualTo(newStartTime);
            assertThat(currentReservation.getEndTime()).isEqualTo(newEndTime);
        }
    }

    @Test
    /* Room pessimistic lock vs @Version */
    void updateRoomConcurrentWithDeleteRoomTest() throws Exception {
        Room targetRoom = roomRepository.save(new Room("202", 20, "Building B", null, null));
        /* update the name */
        String newName = "newName";

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        runConcurrentActions(failures,
                () -> roomService.updateRoom(targetRoom.getId(), newName,
                        targetRoom.getCapacity(), targetRoom.getArea(),
                        targetRoom.getOpenTime(), targetRoom.getCloseTime()),
                () -> roomService.deleteRoom(targetRoom.getId())
        );

        /* unless update and delete run in serial, one operation will fail */
        assertThat(failures).hasSizeLessThanOrEqualTo(1);
        if (!failures.isEmpty()) {
            Throwable failure = failures.peek();
            if (failure instanceof ResourceNotFoundException) {
                /* delete read after update commit, delete simply cannot find the target, no conflict */
                assertThat(failure).hasMessage("Room not found.");
            } else {
                /* write conflict on @Version, one fail with ObjectOptimisticLockingFailureException */
                assertOptimisticLockingFailure(failure);
            }
        }

        /* delete: pessimistic lock, update: @Version
        final state of room will always be deleted */
        Room currentRoom = roomRepository.findById(targetRoom.getId()).orElseThrow();
        assertThat(currentRoom.getStatus()).isEqualTo(RoomStatus.ROOM_STATUS_DELETED);
        /* if update commits first, data might be updated */
        assertThat(currentRoom.getName()).isIn(newName, targetRoom.getName());
    }

    @Test
    /* User pessimistic lock vs @Version */
    void updateUserConcurrentWithDeleteUserTest() throws Exception {
        User targetUser = userRepository.save(new User("targetUser", "password"));

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        runConcurrentActions(failures,
                () -> userService.updateUsername(targetUser.getId(), "newUsername"),
                () -> userService.closeUserAccount(targetUser.getId())
        );

        /* unless update and delete run in serial, one operation will fail */
        assertThat(failures).hasSizeLessThanOrEqualTo(1);
        if (!failures.isEmpty()) {
            Throwable failure = failures.peek();
            if (failure instanceof ResourceNotFoundException) {
                /* close read after update commit, close simply cannot find the target, no conflict */
                assertThat(failure).hasMessage("User not found.");
            } else {
                /* write conflict on @Version, one fails */
                assertOptimisticLockingFailure(failure);
            }
        }

        /* delete: pessimistic lock, update: @Version
        final state of user will always be closed,
        if delete commits first update can no longer modify,
        if update commits first, the username will still be cleared */
        User currentUser = userRepository.findById(targetUser.getId()).orElseThrow();
        assertThat(currentUser)
                .extracting(User::getStatus, User::getUsername, User::getPassword)
                .containsExactly(UserStatus.USER_STATUS_CLOSED, null, null);
    }

    @Test
    /* both with @Version */
    void addClosureConcurrentWithDeleteClosureTest() throws Exception {
        /* initial closure */
        Closure initialClosure = closureRepository.save(new Closure(room,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                LocalDateTime.of(2026, 7, 1, 12, 0)));

        /* new closure to add, which overlap with the initial one and will cause a delete and merge. */
        LocalDateTime newStartTime = LocalDateTime.of(2026, 7, 1, 11, 0);
        LocalDateTime newEndTime = LocalDateTime.of(2026, 7, 1, 13, 0);

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        runConcurrentActions(failures,
                () -> closureService.addClosure(room.getId(), newStartTime, newEndTime),
                () -> closureService.deleteClosure(initialClosure.getId())
        );

        /* unless add and delete run in serial, one operation will fail */
        assertThat(failures).hasSizeLessThanOrEqualTo(1);
        if (!failures.isEmpty()) {
            Throwable failure = failures.peek();
            if (failure instanceof ResourceNotFoundException) {
                /* deleteClosure read after addClosure commit, delete simply cannot find the target, no conflict */
                assertThat(failure).hasMessage("Closure not found.");
            } else {
                /* conflict on @Version, the later one committing deletion fails  */
                assertOptimisticLockingFailure(failure);
            }
        }

        List<Closure> closures = closureRepository.findByRoomId(room.getId());
        /* if conflict on @Version and delete succeeded, add closure fails, no closure left */
        assertThat(closures).hasSizeLessThanOrEqualTo(1);
        /* if no conflict or add closure succeeds, 1 closure left */
        if (!closures.isEmpty()) {
            Closure closure = closures.get(0);
            assertThat(closure.getEndTime()).isEqualTo(newEndTime);
            /* either delete finished first and new closure added with given time, or new closure merged with existing one */
            assertThat(closure.getStartTime()).isIn(initialClosure.getStartTime(), newStartTime);
        }
    }

    private List<Reservation> findReservationsByRoomAndInterval(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return reservationRepository.findByRoomId(roomId).stream()
                .filter(res -> res.getStartTime().equals(startTime)
                        && res.getEndTime().equals(endTime))
                .toList();
    }

    /* coordinator */
    private void runConcurrentActions(
            ConcurrentLinkedQueue<Throwable> failures,
            ConcurrentAction first,
            ConcurrentAction second
    ) throws Exception {
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            executor.execute(() -> runAction(first, readyLatch, startLatch, doneLatch, failures));
            executor.execute(() -> runAction(second, readyLatch, startLatch, doneLatch, failures));

            /* wait for both action to get ready */
            assertThat(readyLatch.await(10, TimeUnit.SECONDS)).isTrue();
            /* release actions to run */
            startLatch.countDown();
            /* wait for both actions to be done */
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdownNow();
        }
    }

    /* action */
    private void runAction(
            ConcurrentAction action,
            CountDownLatch readyLatch,
            CountDownLatch startLatch,
            CountDownLatch doneLatch,
            ConcurrentLinkedQueue<Throwable> failures
    ) {
        try {
            /* confirm ready */
            readyLatch.countDown();
            /* wait for coordinator */
            startLatch.await();
            action.run();
        } catch (Throwable throwable) {
            failures.add(throwable);
        } finally {
            /* announce finished */
            doneLatch.countDown();
        }
    }
}
