package com.zsj.RoomBooking.integration.workflow;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for main business workflows.
 * HTTP/security is tested in a separate test suite and is not involved here.
 */

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.jpa.hibernate.ddl-auto=create-drop"}
)
class ServiceWorkflowIntegrationTest {
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
    private ClosureRepository closureRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-trixie");

    private Room room;
    private User user;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        closureRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        room = roomRepository.save(new Room("101", 12, "Building A", null, null));
        user = userRepository.save(new User("user1", "password"));
    }

    @Test
    void createReservationTest() {
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 0);

        Reservation reservation = reservationService.addReservation(user.getId(), room.getId(), startTime, endTime);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
        assertThat(reservationRepository.findById(reservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStartTime, Reservation::getEndTime, Reservation::getStatus)
                .containsExactly(startTime, endTime, ReservationStatus.RESERVATION_STATUS_ACTIVE);
    }

    @Test
    void updateReservationTest() {
        Reservation reservation = reservationRepository.save(
                new Reservation(user, room,
                        LocalDateTime.of(2300, 3, 1, 10, 0),
                        LocalDateTime.of(2300, 3, 1, 11, 0))
        );
        LocalDateTime newStartTime = LocalDateTime.of(2300, 3, 1, 12, 0);
        LocalDateTime newEndTime = LocalDateTime.of(2300, 3, 1, 13, 0);

        Reservation updatedReservation = reservationService.updateReservationTime(reservation.getId(), newStartTime, newEndTime);

        assertThat(updatedReservation)
                .extracting(Reservation::getStartTime, Reservation::getEndTime, Reservation::getStatus)
                .containsExactly(newStartTime, newEndTime, ReservationStatus.RESERVATION_STATUS_ACTIVE);
        assertThat(reservationRepository.findById(reservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStartTime, Reservation::getEndTime, Reservation::getStatus)
                .containsExactly(newStartTime, newEndTime, ReservationStatus.RESERVATION_STATUS_ACTIVE);
    }

    @Test
    void cancelReservationTest() {
        Reservation reservation = reservationRepository.save(
                new Reservation(user, room,
                        LocalDateTime.of(2300, 3, 1, 10, 0),
                        LocalDateTime.of(2300, 3, 1, 11, 0))
        );

        reservationService.deleteReservation(reservation.getId());

        assertThat(reservationRepository.findById(reservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVATION_STATUS_CANCELED);
    }

    @Test
    void addClosureAffectingReservationsTest() {
        Reservation overlappingReservation = reservationRepository.save(
                new Reservation(user, room,
                        LocalDateTime.of(2300, 3, 1, 10, 0),
                        LocalDateTime.of(2300, 3, 1, 11, 0))
        );
        Reservation unaffectedReservation = reservationRepository.save(
                new Reservation(user, room,
                        LocalDateTime.of(2300, 3, 1, 12, 0),
                        LocalDateTime.of(2300, 3, 1, 13, 0))
        );
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30);

        closureService.addClosure(room.getId(), startTime, endTime);

        assertThat(closureRepository.findByRoomId(room.getId()))
                .singleElement()
                .extracting(Closure::getStartTime, Closure::getEndTime)
                .containsExactly(startTime, endTime);
        assertThat(reservationRepository.findById(overlappingReservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
        assertThat(reservationRepository.findById(unaffectedReservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
    }

    @Test
    void deleteRoomAffectingFutureBookingsTest() {
        LocalDateTime now = LocalDateTime.now();
        Reservation futureReservation = reservationRepository.save(
                new Reservation(user, room,
                        now.plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                        now.plusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0))
        );
        Reservation pastReservation = reservationRepository.save(
                new Reservation(user, room,
                        now.minusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                        now.minusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0))
        );
        closureRepository.save(new Closure(room,
                now.plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0),
                now.plusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0)));
        Closure pastClosure = closureRepository.save(new Closure(room,
                now.minusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0),
                now.minusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0)));

        List<Reservation> closedReservations = roomService.deleteRoom(room.getId());
        assertThat(closedReservations)
                .extracting(Reservation::getId)
                .containsExactly(futureReservation.getId());

        assertThat(roomRepository.findById(room.getId()))
                .isPresent()
                .get()
                .extracting(Room::getStatus)
                .isEqualTo(RoomStatus.ROOM_STATUS_DELETED);
        /* future reservation closed, past reservation unchanged */
        assertThat(reservationRepository.findById(futureReservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
        assertThat(reservationRepository.findById(pastReservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
        /* future closure deleted, past closure remained */
        assertThat(closureRepository.findByRoomId(room.getId()))
                .extracting(Closure::getId)
                .containsExactly(pastClosure.getId());
    }

    @Test
    void closeUserAccountAffectingReservationsTest() {
        Room anotherRoom = roomRepository.save(new Room("102", 8, "Building B", null, null));
        User anotherUser = userRepository.save(new User("user2", "password"));
        LocalDateTime now = LocalDateTime.now();
        Reservation futureReservation = reservationRepository.save(
                new Reservation(user, room,
                        now.plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                        now.plusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0))
        );
        Reservation pastReservation = reservationRepository.save(
                new Reservation(user, room,
                        now.minusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                        now.minusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0))
        );
        Reservation anotherUserReservation = reservationRepository.save(
                new Reservation(anotherUser, anotherRoom,
                        now.plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0),
                        now.plusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0))
        );

        userService.closeUserAccount(user.getId());

        assertThat(userRepository.findById(user.getId()))
                .isPresent()
                .get()
                .extracting(User::getStatus, User::getUsername, User::getPassword)
                .containsExactly(UserStatus.USER_STATUS_CLOSED, null, null);
        assertThat(reservationRepository.findById(futureReservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
        assertThat(reservationRepository.findById(pastReservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
        assertThat(reservationRepository.findById(anotherUserReservation.getId()))
                .isPresent()
                .get()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
    }
}
