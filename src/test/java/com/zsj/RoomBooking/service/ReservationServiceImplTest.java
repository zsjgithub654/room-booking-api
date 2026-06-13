package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.RoomStatus;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {
    @Mock
    private ClosureRepository closureRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void getReservationSucceedTest() {
        Reservation reservation = new Reservation(new User("user1", ""),
                new Room("101", 12, "Building A", null, null),
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
        Long searchId = 2L;
        when(reservationRepository.findById(eq(searchId))).thenReturn(Optional.of(reservation));

        assertThat(reservationService.getReservation(searchId))
                .usingRecursiveComparison()
                .isEqualTo(reservation);
    }

    @Test
    void getReservationNotFoundTest() {
        Long searchId = 2L;
        when(reservationRepository.findById(eq(searchId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.getReservation(searchId));
        assertThat(exception.getMessage()).isEqualTo("Reservation not found.");
    }

    @Test
    void searchReservationsHasResultTest() {
        Long userId = 2L;
        Long roomId = 3L;
        LocalDate date = LocalDate.of(2026, 3, 1);
        ReservationStatus status = ReservationStatus.RESERVATION_STATUS_SCHEDULED;
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        List<Reservation> reservations = List.of(
                new Reservation(user, room,
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0)),
                new Reservation(user, room,
                        LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0)));

        when(reservationRepository.findByUserIdAndRoomIdAndStartTimeAndStatus(
                eq(userId), eq(roomId),
                eq(date.atStartOfDay()), eq(date.plusDays(1).atStartOfDay()),
                eq(status), eq(Pageable.unpaged(getOccupationSort()))))
                .thenReturn(new PageImpl<>(reservations));

        List<Reservation> result = reservationService.searchReservations(userId, roomId, date, status, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(2);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(reservations);
    }

    @Test
    void searchReservationsShouldApplyDefaultSortToPagedQueryTest() {
        Long userId = 2L;
        Long roomId = 3L;
        LocalDate date = LocalDate.of(2026, 3, 1);
        ReservationStatus status = ReservationStatus.RESERVATION_STATUS_SCHEDULED;
        Pageable pageable = PageRequest.of(0, 20);
        Pageable expectedPageable = PageRequest.of(
                0,
                20,
                getOccupationSort());

        when(reservationRepository.findByUserIdAndRoomIdAndStartTimeAndStatus(
                eq(userId), eq(roomId),
                eq(date.atStartOfDay()), eq(date.plusDays(1).atStartOfDay()),
                eq(status),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), expectedPageable, 0));

        reservationService.searchReservations(userId, roomId, date, status, pageable);

        verify(reservationRepository).findByUserIdAndRoomIdAndStartTimeAndStatus(
                userId,
                roomId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                status,
                expectedPageable);
    }

    @Test
    void addReservationSuccessTest() {
        Long userId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        /* id needs to be set since the addReservation method extracts it from the room object */
        ReflectionTestUtils.setField(room, "id", roomId);

        when(userRepository.findByIdWithLock(eq(userId))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(roomId))).thenReturn(Optional.of(room));
        when(closureRepository.existsByRoomIdAndOverlapping(eq(roomId), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.existsByRoomIdAndOverlappingAndScheduled(eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.addReservation(userId, roomId, startTime, endTime);
        assertThat(result.getUser())
                .usingRecursiveComparison()
                .isEqualTo(user);
        assertThat(result.getRoom())
                .usingRecursiveComparison()
                .isEqualTo(room);
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(endTime);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_SCHEDULED);
    }

    @Test
    void addReservationNotInOpenHourTest() {
        Long userId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 8, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 9, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", LocalTime.of(9, 0), LocalTime.of(17, 0));
        ReflectionTestUtils.setField(room, "id", roomId);

        when(userRepository.findByIdWithLock(eq(userId))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(roomId))).thenReturn(Optional.of(room));

        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.addReservation(userId, roomId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room is not in open hours during selected time.");
    }

    @Test
    void addReservationOpenAllDayCanCrossDayTest() {
        Long userId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 23, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 2, 1, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);

        when(userRepository.findByIdWithLock(eq(userId))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(roomId))).thenReturn(Optional.of(room));
        when(closureRepository.existsByRoomIdAndOverlapping(eq(roomId), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.existsByRoomIdAndOverlappingAndScheduled(eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.addReservation(userId, roomId, startTime, endTime);
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(endTime);
    }

    @Test
    void addReservationUserNotFoundTest() {
        Long userId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0);

        when(userRepository.findByIdWithLock(eq(userId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.addReservation(userId, roomId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("User not found.");
    }

    @Test
    void addReservationRoomNotFoundTest() {
        Long userId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0);
        User user = new User("user1", "");

        when(userRepository.findByIdWithLock(eq(userId))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(roomId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.addReservation(userId, roomId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room not found.");
    }

    @Test
    void addReservationConflictWithClosureTest() {
        Long userId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);

        when(userRepository.findByIdWithLock(eq(userId))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(roomId))).thenReturn(Optional.of(room));
        when(closureRepository.existsByRoomIdAndOverlapping(eq(roomId), eq(startTime), eq(endTime))).thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.addReservation(userId, roomId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room is in closure during selected time.");
    }

    @Test
    void addReservationConflictWithReservationTest() {
        Long userId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);

        when(userRepository.findByIdWithLock(eq(userId))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(roomId))).thenReturn(Optional.of(room));
        when(closureRepository.existsByRoomIdAndOverlapping(eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(false);
        when(reservationRepository.existsByRoomIdAndOverlappingAndScheduled(eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.addReservation(userId, roomId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room is reserved in selected time.");
    }

    @Test
    void releaseReservationBeforeStartTest() {
        Long reservationId = 2L;
        Reservation reservation = new Reservation(new User("user1", ""),
                new Room("101", 12, "Building A", null, null),
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));

        reservationService.releaseReservation(reservationId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CANCELED);
    }

    @Test
    void releaseReservationDuringReservationTest() {
        Long reservationId = 2L;
        LocalDateTime beforeRelease = LocalDateTime.now();
        LocalDateTime startTime = beforeRelease.minusMinutes(1).withSecond(0).withNano(0);
        Reservation reservation = new Reservation(new User("user1", ""),
                new Room("101", 12, "Building A", null, null),
                startTime,
                beforeRelease.plusMinutes(29));

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));

        reservationService.releaseReservation(reservationId);

        LocalDateTime afterRelease = LocalDateTime.now();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_SCHEDULED);
        assertThat(reservation.getStartTime()).isEqualTo(startTime);
        assertThat(reservation.getEndTime()).isBetween(
                getReleasedEndTime(beforeRelease),
                getReleasedEndTime(afterRelease));
    }

    @Test
    void releaseReservationEndedTest() {
        Long reservationId = 2L;
        Reservation reservation = new Reservation(new User("user1", ""),
                new Room("101", 12, "Building A", null, null),
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().minusMinutes(1));

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));

        reservationService.releaseReservation(reservationId);
        assertThat(reservation.isScheduled()).isTrue();
    }

    @Test
    void releaseReservationCanceledTest() {
        Long reservationId = 2L;
        Reservation reservation = new Reservation(new User("user1", ""),
                new Room("101", 12, "Building A", null, null),
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));

        reservationService.releaseReservation(reservationId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CANCELED);
    }

    @Test
    void releaseReservationClosedTest() {
        Long reservationId = 2L;
        Reservation reservation = new Reservation(new User("user1", ""),
                new Room("101", 12, "Building A", null, null),
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CLOSED);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));

        reservationService.releaseReservation(reservationId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
    }

    @Test
    void releaseReservationNotFoundTest() {
        Long reservationId = 2L;
        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.releaseReservation(reservationId));
        assertThat(exception.getMessage()).isEqualTo("Reservation not found.");
    }

    @Test
    void updateReservationTimeSucceedTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(room.getId()))).thenReturn(Optional.of(room));
        when(closureRepository.existsByRoomIdAndOverlapping(eq(room.getId()), eq(startTime), eq(endTime)))
                .thenReturn(false);
        when(reservationRepository.existsByRoomIdAndOverlappingAndScheduledExcludingReservation(
                eq(room.getId()),
                eq(reservationId),
                eq(startTime),
                eq(endTime)))
                .thenReturn(false);

        Reservation result = reservationService.updateReservationTime(reservationId, startTime, endTime);
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(endTime);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_SCHEDULED);
    }

    @Test
    void updateReservationTimeWithinOriginalRangeTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 15, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 10, 45, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", LocalTime.of(12, 0), LocalTime.of(17, 0));
        ReflectionTestUtils.setField(room, "id", roomId);
        ReflectionTestUtils.setField(user, "id", 4L);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(roomId))).thenReturn(Optional.of(room));

        Reservation result = reservationService.updateReservationTime(reservationId, startTime, endTime);

        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(endTime);
        verify(closureRepository, never()).existsByRoomIdAndOverlapping(any(), any(), any());
        verify(reservationRepository, never()).existsByRoomIdAndOverlappingAndScheduledExcludingReservation(
                any(), any(), any(), any());
    }

    @Test
    void updateReservationStartedTest() {
        Long reservationId = 2L;
        Reservation reservation = new Reservation(new User("user1", ""),
                new Room("101", 12, "Building A", null, null),
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(29));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.updateReservationTime(
                        reservationId,
                        LocalDateTime.of(2300, 3, 1, 12, 0, 0, 0),
                        LocalDateTime.of(2300, 3, 1, 13, 0, 0, 0)));
        assertThat(exception.getMessage()).isEqualTo("Started reservation cannot be updated.");
    }

    @Test
    void updateReservationOutsideRoomHoursTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 18, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 19, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", LocalTime.of(9, 0), LocalTime.of(17, 0));
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(roomId))).thenReturn(Optional.of(room));

        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room is not in open hours during selected time.");
    }

    @Test
    void updateReservationReservationNotFoundTest() {
        Long reservationId = 2L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 13, 0, 0, 0);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Reservation not found.");
    }

    @Test
    void updateReservationUserNotFoundTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("User not found.");
    }

    @Test
    void updateReservationInactiveUserTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        user.setStatus(UserStatus.USER_STATUS_CLOSED);
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("User not found.");
    }

    @Test
    void updateReservationRoomNotFoundTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(room.getId()))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room not found.");
    }

    @Test
    void updateReservationInactiveRoomTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        room.setStatus(RoomStatus.ROOM_STATUS_DELETED);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(room.getId()))).thenReturn(Optional.of(room));

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room not found.");
    }

    @Test
    void updateReservationConflictWithClosureTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(room.getId()))).thenReturn(Optional.of(room));
        when(closureRepository.existsByRoomIdAndOverlapping(eq(room.getId()), eq(startTime), eq(endTime)))
                .thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room is in closure during selected time.");
    }

    @Test
    void updateReservationConflictWithReservationTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(room.getId()))).thenReturn(Optional.of(room));
        when(closureRepository.existsByRoomIdAndOverlapping(eq(room.getId()), eq(startTime), eq(endTime)))
                .thenReturn(false);
        when(reservationRepository.existsByRoomIdAndOverlappingAndScheduledExcludingReservation(
                eq(room.getId()),
                eq(reservationId),
                eq(startTime),
                eq(endTime)))
                .thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room is reserved in selected time.");
    }

    private Sort getOccupationSort() {
        return Sort.by(
                Sort.Order.asc("startTime"),
                Sort.Order.asc("endTime"),
                Sort.Order.asc("id"));
    }

    private LocalDateTime getReleasedEndTime(LocalDateTime currentTime) {
        if (currentTime.getSecond() == 0 && currentTime.getNano() == 0) {
            return currentTime;
        }
        return currentTime.plusMinutes(1).withSecond(0).withNano(0);
    }
}
