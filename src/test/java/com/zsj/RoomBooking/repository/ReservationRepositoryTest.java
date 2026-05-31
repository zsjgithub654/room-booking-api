package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
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
public class ReservationRepositoryTest {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Room room;

    @BeforeEach
    /* DataJpaTest rollback DB after each test */
    public void setup() {
        room = roomRepository.save(new Room("101", 12, "building A", null, null));
        user = userRepository.save(new User("user1", ""));
    }

    /* findByRoomId */
    @Test
    void findByRoomIdHasResultTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> reservationsRetrieved = reservationRepository.findByRoomId(room.getId());
        assertThat(reservationsRetrieved).hasSize(1);
        assertThat(reservationsRetrieved.get(0))
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(reservation);
    }

    @Test
    void findByRoomIdNoResultTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> reservationsRetrieved = reservationRepository.findByRoomId(room.getId() + 1);
        assertThat(reservationsRetrieved).hasSize(0);
    }

    /* getTimeByRoomIdAndOverlappingIntervalAndActive */
    @Test
    void findByRoomIdAndOverlappingAndActiveHasResultTest() {
        List<Reservation> reservations = List.of(
                /* startTime < lowerBound, lowerBound < end Time < upperBound */
                new Reservation(user, room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 5, 1, 15, 30, 0, 0)),
                /* lowerBound < end Time < upperBound, lowerBound < endTime < upperBound */
                new Reservation(user, room,
                        LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0)),
                /* lowerBound < end Time < upperBound, end Time > upperBound */
                new Reservation(user, room,
                        LocalDateTime.of(2026, 6, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0)),
                /* startTime < lowerBound, end Time > upperBound */
                new Reservation(user, room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0))
        );
        reservationRepository.saveAll(reservations);

        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(4);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(reservations);
    }

    @Test
    void findByRoomIdAndOverlappingIntervalAndActiveNoResultMatchRoomTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndActive(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingIntervalAndActiveNoResultMatchTimeTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndActive(room.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 8, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingAndActiveNoResultMatchStatusTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED);
        reservationRepository.save(reservation);

        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(result).hasSize(0);
    }

    /* findByRoomIdAndStartAfterAndActive */
    @Test
    void findByRoomIdAndStartAfterAndActiveHasResultTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0));
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0)).isEqualTo(reservation);
    }

    @Test
    void findByRoomIdAndStartAfterAndActiveNoResultMatchRoomTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> queryResult = reservationRepository.findByRoomIdAndStartAfterAndActive(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0));
        assertThat(queryResult).hasSize(0);
    }

    @Test
    void findByRoomIdAndStartAfterAndActiveNoResultMatchTimeTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> queryResult = reservationRepository.findByRoomIdAndStartAfterAndActive(room.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(queryResult).hasSize(0);
    }

    @Test
    void findByRoomIdAndStartAfterAndActiveNoResultMatchStatusTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED);
        reservationRepository.save(reservation);

        List<Reservation> queryResult = reservationRepository.findByRoomIdAndStartAfterAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0));
        assertThat(queryResult).hasSize(0);
    }

    /* findByUserIdAndStartAfterAndActive */
    @Test
    void findByUserIdAndStartAfterAndActiveHasResultTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> reservations = reservationRepository.findByUserIdAndStartAfterAndActive(user.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0));
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0)).isEqualTo(reservation);
    }

    @Test
    void findByUserIdAndStartAfterAndActiveNoResultMatchRoomTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> queryResult = reservationRepository.findByUserIdAndStartAfterAndActive(user.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0));
        assertThat(queryResult).hasSize(0);
    }

    @Test
    void findByUserIdAndStartAfterAndActiveNoResultMatchTimeTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservationRepository.save(reservation);

        List<Reservation> queryResult = reservationRepository.findByUserIdAndStartAfterAndActive(user.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0));
        assertThat(queryResult).hasSize(0);
    }

    @Test
    void findByUserIdAndStartAfterAndActiveNoResultMatchStatusTest() {
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0));
        reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED);
        reservationRepository.save(reservation);

        List<Reservation> queryResult = reservationRepository.findByUserIdAndStartAfterAndActive(user.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0));
        assertThat(queryResult).hasSize(0);
    }
}
