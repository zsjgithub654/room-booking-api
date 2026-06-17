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
import org.springframework.data.domain.Sort;

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

    /* ReservationSpecifications */
    @Test
    void filterByUserIdTest() {
        List<Reservation> result = reservationRepository.findAll(
                ReservationSpecifications.hasUserId(user.getId()));

        assertThat(result).hasSize(reservations.size());
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(reservations);
    }

    @Test
    void filterByRoomIdTest() {
        List<Reservation> result = reservationRepository.findAll(
                ReservationSpecifications.hasRoomId(room.getId()));

        assertThat(result).hasSize(reservations.size());
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(reservations);
    }

    @Test
    void filterByStartAtOrAfterTest() {
        List<Reservation> result = reservationRepository.findAll(
                ReservationSpecifications.startsAtOrAfter(LocalDateTime.of(2026, 6, 1, 0, 0, 0, 0)),
                getOccupationSort());

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(List.of(reservations.get(1), reservations.get(2)));
    }

    @Test
    void filterByStartBeforeTest() {
        List<Reservation> result = reservationRepository.findAll(
                ReservationSpecifications.startsBefore(LocalDateTime.of(2026, 6, 2, 0, 0, 0, 0)),
                getOccupationSort());

        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(List.of(reservations.get(0), reservations.get(3), reservations.get(1)));
    }

    @Test
    void filterByStatusTest() {
        reservations.get(1).setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED);
        reservationRepository.saveAll(reservations);

        List<Reservation> result = reservationRepository.findAll(
                ReservationSpecifications.hasStatus(ReservationStatus.RESERVATION_STATUS_CANCELED));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(reservations.get(1));
    }

    @Test
    void findAllWithCombinedReservationSpecificationsHasResultTest() {
        List<Reservation> result = reservationRepository.findAll(
                ReservationSpecifications.hasUserId(user.getId())
                        .and(ReservationSpecifications.hasRoomId(room.getId()))
                        .and(ReservationSpecifications.startsAtOrAfter(LocalDateTime.of(2026, 6, 1, 0, 0, 0, 0)))
                        .and(ReservationSpecifications.startsBefore(LocalDateTime.of(2026, 6, 2, 0, 0, 0, 0)))
                        .and(ReservationSpecifications.hasStatus(ReservationStatus.RESERVATION_STATUS_SCHEDULED)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(reservations.get(1));
    }

    @Test
    void findAllWithCombinedReservationSpecificationsPagedTest() {
        Page<Reservation> result = reservationRepository.findAll(
                ReservationSpecifications.hasUserId(user.getId())
                        .and(ReservationSpecifications.hasRoomId(room.getId()))
                        .and(ReservationSpecifications.hasStatus(ReservationStatus.RESERVATION_STATUS_SCHEDULED)),
                PageRequest.of(0, 2, Sort.by("startTime")));

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Reservation::getStartTime)
                .containsExactly(
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 4, 30, 14, 30, 0, 0));
    }

    /* getTimeByRoomIdAndOverlappingIntervalAndScheduled */
    @Test
    void findByRoomIdAndOverlappingAndScheduledHasResultTest() {
        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndScheduled(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                getOccupationSort());
        assertThat(result).hasSize(4);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(reservations.get(0), reservations.get(3), reservations.get(1), reservations.get(2)));
    }

    @Test
    void findByRoomIdAndOverlappingAndScheduledNoResultMatchRoomTest() {
        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndScheduled(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(result).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingAndScheduledNoResultMatchTimeTest() {
        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndScheduled(room.getId(),
                LocalDateTime.of(2026, 8, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(result).hasSize(0);
    }

    @Test
    void findByRoomIdAndOverlappingAndScheduledNoResultMatchStatusTest() {
        reservations.forEach(reservation -> reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED));
        reservationRepository.saveAll(reservations);

        List<Reservation> result = reservationRepository.findByRoomIdAndOverlappingAndScheduled(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(result).hasSize(0);
    }

    @Test
    void existsByRoomIdAndOverlappingAndScheduledExistsTest() {
        assertThat(reservationRepository.existsByRoomIdAndOverlappingAndScheduled(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0))
        ).isTrue();
    }

    @Test
    void existsByRoomIdAndOverlappingAndScheduledNoResultMatchRoomTest() {
        assertThat(reservationRepository.existsByRoomIdAndOverlappingAndScheduled(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0))
        ).isFalse();
    }

    @Test
    void existsByRoomIdAndOverlappingAndScheduledNoResultMatchTimeTest() {
        assertThat(reservationRepository.existsByRoomIdAndOverlappingAndScheduled(room.getId(),
                LocalDateTime.of(2026, 8, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0, 0, 0))
        ).isFalse();
    }

    @Test
    void existsByRoomIdAndOverlappingAndScheduledNoResultMatchStatusTest() {
        reservations.forEach(reservation -> reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED));
        reservationRepository.saveAll(reservations);

        assertThat(reservationRepository.existsByRoomIdAndOverlappingAndScheduled(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0))
        ).isFalse();
    }

    /* findByRoomIdAndStartAfterAndScheduled */
    @Test
    void findByRoomIdAndStartAfterAndScheduledHasResultTest() {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndScheduled(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                getOccupationSort());
        assertThat(reservations).hasSize(2);
        assertThat(reservations).isEqualTo(reservations);
    }

    @Test
    void findByRoomIdAndStartAfterAndScheduledNoResultMatchRoomTest() {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndScheduled(room.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(reservations).hasSize(0);
    }

    @Test
    void findByRoomIdAndStartAfterAndScheduledNoResultMatchTimeTest() {
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndScheduled(room.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(reservations).hasSize(0);
    }

    @Test
    void findByRoomIdAndStartAfterAndScheduledNoResultMatchStatusTest() {
        reservations.forEach(reservation -> reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED));
        reservationRepository.saveAll(reservations);
        List<Reservation> queryResult = reservationRepository.findByRoomIdAndStartAfterAndScheduled(room.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(queryResult).hasSize(0);
    }

    /* findByUserIdAndStartAfterAndScheduled */
    @Test
    void findByUserIdAndStartAfterAndScheduledHasResultTest() {
        List<Reservation> reservations = reservationRepository.findByUserIdAndStartAfterAndScheduled(user.getId(),
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                getOccupationSort());
        assertThat(reservations).hasSize(2);
        assertThat(reservations).isEqualTo(reservations);
    }

    @Test
    void findByUserIdAndStartAfterAndScheduledNoResultMatchRoomTest() {
        List<Reservation> queryResult = reservationRepository.findByUserIdAndStartAfterAndScheduled(user.getId() + 1,
                LocalDateTime.of(2026, 5, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(queryResult).hasSize(0);
    }

    @Test
    void findByUserIdAndStartAfterAndScheduledNoResultMatchTimeTest() {
        List<Reservation> queryResult = reservationRepository.findByUserIdAndStartAfterAndScheduled(user.getId(),
                LocalDateTime.of(2026, 7, 1, 0, 0, 0, 0),
                Sort.unsorted());
        assertThat(queryResult).hasSize(0);
    }

    @Test
    void findByUserIdAndStartAfterAndScheduledNoResultMatchStatusTest() {
        reservations.forEach(reservation -> reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED));
        reservationRepository.saveAll(reservations);

        List<Reservation> queryResult = reservationRepository.findByUserIdAndStartAfterAndScheduled(user.getId(),
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
