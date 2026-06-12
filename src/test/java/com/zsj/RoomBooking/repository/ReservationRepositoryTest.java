package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
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
    private List<Reservation> reservations;

    @BeforeEach
    /* DataJpaTest rollback DB after each test */
    public void setup() {
        room = roomRepository.save(new Room("101", 12, "building A", null, null));
        user = userRepository.save(new User("user1", ""));
        reservations = reservationRepository.saveAll(List.of(
                new Reservation(user, room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 5, 1, 15, 30, 0, 0)),
                new Reservation(user, room,
                        LocalDateTime.of(2026, 6, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 15, 30, 0, 0)),
                new Reservation(user, room,
                        LocalDateTime.of(2026, 6, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0)),
                new Reservation(user, room,
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 7, 1, 15, 30, 0, 0))
        ));
    }

    /* findByRoomId */
    @Test
    void findByRoomIdHasResultTest() {
        List<Reservation> reservationsRetrieved = reservationRepository.findByRoomId(room.getId());
        assertThat(reservationsRetrieved).hasSize(reservations.size());
        assertThat(reservationsRetrieved)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(reservations);
    }

    @Test
    void findByRoomIdNoResultTest() {
        List<Reservation> reservationsRetrieved = reservationRepository.findByRoomId(room.getId() + 1);
        assertThat(reservationsRetrieved).hasSize(0);
    }

    /* findByUserIdAndRoomIdAndDateAndStatus */
    @Test
    void findByUserIdAndRoomIdAndDateAndStatusHasResultTest() {
        List<Reservation> result = reservationRepository.findByUserIdAndRoomIdAndDateAndStatus(
                user.getId(),
                room.getId(),
                LocalDate.of(2026, 6, 1),
                ReservationStatus.RESERVATION_STATUS_ACTIVE,
                Pageable.unpaged()).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(reservations.get(1));
    }

    @Test
    void findByUserIdAndRoomIdAndDateAndStatusNoResultMatchRoomTest() {
        List<Reservation> result = reservationRepository.findByUserIdAndRoomIdAndDateAndStatus(
                user.getId(),
                room.getId() + 1,
                LocalDate.of(2026, 6, 1),
                ReservationStatus.RESERVATION_STATUS_ACTIVE,
                Pageable.unpaged()).getContent();
        assertThat(result).hasSize(0);
    }

    @Test
    void findByUserIdAndRoomIdAndDateAndStatusNoResultMatchUserTest() {
        List<Reservation> result = reservationRepository.findByUserIdAndRoomIdAndDateAndStatus(
                user.getId() + 1,
                room.getId(),
                LocalDate.of(2026, 6, 1),
                ReservationStatus.RESERVATION_STATUS_ACTIVE,
                Pageable.unpaged()).getContent();
        assertThat(result).hasSize(0);
    }

    @Test
    void findByUserIdAndRoomIdAndDateAndStatusNoResultMatchDateTest() {
        List<Reservation> result = reservationRepository.findByUserIdAndRoomIdAndDateAndStatus(
                user.getId(),
                room.getId(),
                LocalDate.of(2026, 7, 1),
                ReservationStatus.RESERVATION_STATUS_ACTIVE,
                Pageable.unpaged()).getContent();
        assertThat(result).hasSize(0);
    }

    @Test
    void findByUserIdAndRoomIdAndDateAndStatusNoResultMatchStatusTest() {
        reservations.get(1).setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED);
        reservationRepository.saveAll(reservations);

        List<Reservation> result = reservationRepository.findByUserIdAndRoomIdAndDateAndStatus(
                user.getId(),
                room.getId(),
                LocalDate.of(2026, 6, 1),
                ReservationStatus.RESERVATION_STATUS_ACTIVE,
                Pageable.unpaged()).getContent();
        assertThat(result).hasSize(0);
    }

    @Test
    void findByUserIdAndRoomIdAndDateAndStatusAllNullArgsTest() {
        List<Reservation> result = reservationRepository.findByUserIdAndRoomIdAndDateAndStatus(
                null, null, null, null, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(reservations.size());
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(reservations);
    }

    @Test
    void findByUserIdAndRoomIdAndDateAndStatusPagedTest() {
        Page<Reservation> result = reservationRepository.findByUserIdAndRoomIdAndDateAndStatus(
                user.getId(),
                room.getId(),
                null,
                ReservationStatus.RESERVATION_STATUS_ACTIVE,
                PageRequest.of(0, 2, Sort.by("startTime")));

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Reservation::getStartTime)
                .containsExactly(
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0));
    }

    /* getTimeByRoomIdAndOverlappingIntervalAndActive */
    @Test
    void findByRoomIdAndOverlappingAndActiveHasResultTest() {
        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                getOccupationSort());
        assertThat(result).hasSize(4);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(reservations.get(0), reservations.get(3), reservations.get(1), reservations.get(2)));
    }

    @Test
    void findByRoomIdAndOverlappingAndActiveNoResultMatchRoomTest() {
        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndActive(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(result).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingAndActiveNoResultMatchTimeTest() {
        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndActive(room.getId(),
                LocalDateTime.of(2026, 8, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(result).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingAndActiveNoResultMatchStatusTest() {
        reservations.forEach(reservation -> reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED));
        reservationRepository.saveAll(reservations);

        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(result).hasSize(0);
    }

    @Test
    void existsByRoomIdAndOverlappingAndActiveExistsTest() {
        assertThat(reservationRepository.existsByRoomIdAndOverlappingAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0))
        ).isTrue();
    }

    @Test
    void existsByRoomIdAndOverlappingAndActiveNoResultMatchRoomTest() {
        assertThat(reservationRepository.existsByRoomIdAndOverlappingAndActive(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0))
        ).isFalse();
    }

    @Test
    void existsByRoomIdAndOverlappingAndActiveNoResultMatchTimeTest() {
        assertThat(reservationRepository.existsByRoomIdAndOverlappingAndActive(room.getId(),
                LocalDateTime.of(2026, 8, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0, 0, 0))
        ).isFalse();
    }

    @Test
    void existsByRoomIdAndOverlappingAndActiveNoResultMatchStatusTest() {
        reservations.forEach(reservation -> reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED));
        reservationRepository.saveAll(reservations);

        assertThat(reservationRepository.existsByRoomIdAndOverlappingAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0))
        ).isFalse();
    }

    /* findByRoomIdAndStartAfterAndActive */
    @Test
    void findByRoomIdAndStartAfterAndActiveHasResultTest() {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                getOccupationSort());
        assertThat(reservations).hasSize(2);
        assertThat(reservations).isEqualTo(reservations);
    }

    @Test
    void findByRoomIdAndStartAfterAndActiveNoResultMatchRoomTest() {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndActive(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(reservations).hasSize(0);
    }

    @Test
    void findByRoomIdAndStartAfterAndActiveNoResultMatchTimeTest() {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndActive(room.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(reservations).hasSize(0);
    }

    @Test
    void findByRoomIdAndStartAfterAndActiveNoResultMatchStatusTest() {
        reservations.forEach(reservation -> reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED));
        reservationRepository.saveAll(reservations);
        List<Reservation> queryResult = reservationRepository.findByRoomIdAndStartAfterAndActive(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(queryResult).hasSize(0);
    }

    /* findByUserIdAndStartAfterAndActive */
    @Test
    void findByUserIdAndStartAfterAndActiveHasResultTest() {
        List<Reservation> reservations = reservationRepository.findByUserIdAndStartAfterAndActive(user.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                getOccupationSort());
        assertThat(reservations).hasSize(2);
        assertThat(reservations).isEqualTo(reservations);
    }

    @Test
    void findByUserIdAndStartAfterAndActiveNoResultMatchRoomTest() {
        List<Reservation> queryResult = reservationRepository.findByUserIdAndStartAfterAndActive(user.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(queryResult).hasSize(0);
    }

    @Test
    void findByUserIdAndStartAfterAndActiveNoResultMatchTimeTest() {
        List<Reservation> queryResult = reservationRepository.findByUserIdAndStartAfterAndActive(user.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(queryResult).hasSize(0);
    }

    @Test
    void findByUserIdAndStartAfterAndActiveNoResultMatchStatusTest() {
        reservations.forEach(reservation -> reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED));
        reservationRepository.saveAll(reservations);

        List<Reservation> queryResult = reservationRepository.findByUserIdAndStartAfterAndActive(user.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(queryResult).hasSize(0);
    }

    private Sort getOccupationSort() {
        return Sort.by(
                Sort.Order.asc("startTime"),
                Sort.Order.asc("endTime"),
                Sort.Order.asc("id"));
    }
}
