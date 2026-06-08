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
        ReservationStatus status = ReservationStatus.RESERVATION_STATUS_ACTIVE;
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        List<Reservation> reservations = List.of(
                new Reservation(user, room,
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0)),
                new Reservation(user, room,
                        LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0)));

        when(reservationRepository.findByUserIdAndRoomIdAndDateAndStatus(eq(userId), eq(roomId), eq(date), eq(status)))
                .thenReturn(reservations);

        List<Reservation> result = reservationService.searchReservations(userId, roomId, date, status);
        assertThat(result).hasSize(2);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(reservations);
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
        when(reservationRepository.existsByRoomIdAndOverlappingAndActive(eq(roomId), eq(startTime), eq(endTime)))
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
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
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
        when(reservationRepository.existsByRoomIdAndOverlappingAndActive(eq(roomId), eq(startTime), eq(endTime)))
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
        when(reservationRepository.existsByRoomIdAndOverlappingAndActive(eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.addReservation(userId, roomId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room is reserved in selected time.");
    }

    @Test
    void deleteReservationSucceedTest() {
        Long reservationId = 2L;
        Reservation reservation = new Reservation(new User("user1", ""),
                new Room("101", 12, "Building A", null, null),
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(reservationId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CANCELED);
    }

    @Test
    void deleteReservationNotFoundTest() {
        Long reservationId = 2L;
        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.deleteReservation(reservationId));
        assertThat(exception.getMessage()).isEqualTo("Reservation not found.");
    }

    @Test
    void updateReservationTimeSucceedTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 30, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(room.getId()))).thenReturn(Optional.of(room));
        when(closureRepository.existsByRoomIdAndOverlapping(eq(room.getId()), eq(startTime), eq(endTime)))
                .thenReturn(false);
        when(reservationRepository.existsByRoomIdAndOverlappingAndActiveExcludingReservation(
                eq(room.getId()),
                eq(reservationId),
                eq(startTime),
                eq(endTime)))
                .thenReturn(false);

        Reservation result = reservationService.updateReservationTime(reservationId, startTime, endTime);
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(endTime);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
    }

    @Test
    void updateReservationOutsideRoomHoursTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 18, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 19, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", LocalTime.of(9, 0), LocalTime.of(17, 0));
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
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
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Reservation not found.");
    }

    @Test
    void updateReservationUserNotFoundTest() {
        Long reservationId = 2L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
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
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        user.setStatus(UserStatus.USER_STATUS_CLOSED);
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
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
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
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
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        room.setStatus(RoomStatus.ROOM_STATUS_DELETED);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
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
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
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
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0);
        User user = new User("user1", "");
        Room room = new Room("101", 12, "Building A", null, null);
        ReflectionTestUtils.setField(room, "id", roomId);
        Reservation reservation = new Reservation(user, room,
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(reservation));
        when(userRepository.findByIdWithLock(eq(user.getId()))).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(eq(room.getId()))).thenReturn(Optional.of(room));
        when(closureRepository.existsByRoomIdAndOverlapping(eq(room.getId()), eq(startTime), eq(endTime)))
                .thenReturn(false);
        when(reservationRepository.existsByRoomIdAndOverlappingAndActiveExcludingReservation(
                eq(room.getId()),
                eq(reservationId),
                eq(startTime),
                eq(endTime)))
                .thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.updateReservationTime(reservationId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room is reserved in selected time.");
    }
}
