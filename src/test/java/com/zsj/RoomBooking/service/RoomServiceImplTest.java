package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.RoomSchedule;
import com.zsj.RoomBooking.model.Occupation;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.RoomStatus;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/* to integrate Mockito */
@ExtendWith(MockitoExtension.class)
public class RoomServiceImplTest {
    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ClosureRepository closureRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    void GetRoomSucceedTest() {
        Room room = new Room("101", 12, "Building A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));
        Long searchId = 2L;
        when(roomRepository.findById(eq(searchId))).thenReturn(Optional.of(room));
        assertThat(roomService.getRoom(searchId))
                .usingRecursiveComparison()
                .isEqualTo(room);
    }

    @Test
    void GetRoomNotFoundTest() {
        Long searchId = 2L;
        when(roomRepository.findById(eq(searchId))).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> roomService.getRoom(searchId));
        assertThat(exception.getMessage()).isEqualTo("Room not found.");
    }

    @Test
    void AddRoomTest() {
        Room room = new Room("101", 12, "Building A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));
        doAnswer(returnsFirstArg()).when(roomRepository).save(eq(room));
        assertThat(roomService.addRoom(room))
                .usingRecursiveComparison()
                .isEqualTo(room);
    }

    @Test
    void UpdateRoomSucceedTest() {
        Room room = new Room("101", 12, "Building A", null, null);
        Room newRoom = new Room("102", 10, "Building 1",
                LocalTime.of(8, 30, 0, 0),
                null);
        Long searchId = 2L;
        when(roomRepository.findById(eq(searchId))).thenReturn(Optional.of(room));
        assertThat(roomService.updateRoom(searchId,
                newRoom.getName(),
                newRoom.getCapacity(),
                newRoom.getArea(),
                newRoom.getOpenTime(),
                newRoom.getCloseTime()))
                .usingRecursiveComparison()
                .isEqualTo(newRoom);
    }

    @Test
    void UpdateRoomNotFoundTest() {
        Room newRoom = new Room("102", 10, "Building 1", null, null);
        Long searchId = 2L;

        when(roomRepository.findById(eq(searchId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> roomService.updateRoom(searchId,
                        newRoom.getName(),
                        newRoom.getCapacity(),
                        newRoom.getArea(),
                        newRoom.getOpenTime(),
                        newRoom.getCloseTime()));
        assertThat(exception.getMessage()).isEqualTo("Room not found.");
    }

    @Test
    void SearchRoomsTest() {
        List<Room> rooms = List.of(
                new Room("101", 12, "Building A", null, null),
                new Room("102", 4, "Building A", null, null),
                new Room("101", 6, "Building B", null, null)
        );
        when(roomRepository.findAll(any(Specification.class))).thenReturn(rooms);
        List<Room> result = roomService.searchRooms(null, null, null, null);
        assertThat(result).hasSize(3);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(rooms);
    }

    @Test
    void searchAvailabilitiesHasResultTest() {
        Room room = new Room("101", 12, "Building A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));
        List<Reservation> reservations = List.of(
                new Reservation(new User(), room,
                        LocalDateTime.of(2026, 3, 1, 8, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0)),
                new Reservation(new User(), room,
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0)));
        List<Closure> closures = List.of(
                new Closure(room,
                        LocalDateTime.of(2026, 3, 1, 14, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 2, 14, 0, 0, 0)));
        LocalDateTime searchFromTime = LocalDateTime.of(2026, 3, 1, 6, 0, 0, 0);
        LocalDateTime searchToTime = LocalDateTime.of(2026, 3, 2, 6, 0, 0, 0);
        /* mock */
        when(roomRepository.findAll(any(Specification.class))).thenReturn(List.of(room));
        when(closureRepository.findByRoomIdAndOverlapping(eq(null), eq(searchFromTime), eq(searchToTime)))
                .thenReturn(closures);
        when(reservationRepository.findByRoomIdAndOverlappingAndActive(eq(null), eq(searchFromTime), eq(searchToTime)))
                .thenReturn(reservations);
        /* verify */
        /* search range start and end time don't overlapped with occupations in between */
        List<RoomSchedule> result = roomService.searchAvailabilities(
                null, null, null, null, searchFromTime, searchToTime);
        assertThat(result).hasSize(1);
        List<Occupation> expectedOccupations = new ArrayList<>();
        expectedOccupations.addAll(reservations);
        expectedOccupations.addAll(closures);
        assertThat(result.get(0).getRoom())
                .usingRecursiveComparison()
                .isEqualTo(room);
        assertThat(result.get(0).getOccupations()).hasSize(reservations.size() + closures.size());
        assertThat(result.get(0).getOccupations())
                .usingRecursiveComparison()
                .isEqualTo(expectedOccupations);
    }
    @Test
    void searchAvailabilitiesFullyOccupiedNoResultTest() {
        Room room = new Room("101", 12, "Building A",
                LocalTime.of(8, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));
        List<Reservation> reservations = List.of(
                new Reservation(new User(), room,
                        LocalDateTime.of(2026, 3, 1, 8, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0)),
                new Reservation(new User(), room,
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 14, 0, 0, 0)));
        List<Closure> closures = List.of(
                new Closure(room,
                        LocalDateTime.of(2026, 3, 1, 14, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 16, 0, 0, 0)));
        LocalDateTime searchFromTime = LocalDateTime.of(2026, 3, 1, 6, 0, 0, 0);
        LocalDateTime searchToTime = LocalDateTime.of(2026, 3, 1, 18, 0, 0, 0);
        /* mock */
        when(roomRepository.findAll(any(Specification.class))).thenReturn(List.of(room));
        when(closureRepository.findByRoomIdAndOverlapping(eq(null), eq(searchFromTime), eq(searchToTime)))
                .thenReturn(closures);
        when(reservationRepository.findByRoomIdAndOverlappingAndActive(eq(null), eq(searchFromTime), eq(searchToTime)))
                .thenReturn(reservations);
        /* verify */
        List<RoomSchedule> result = roomService.searchAvailabilities(
                null, null, null, null, searchFromTime, searchToTime);
        assertThat(result).hasSize(0);
    }

    @Test
    void searchAvailabilitiesNotInOpenHourNoResultTest() {
        Room room = new Room("101", 12, "Building A",
                LocalTime.of(8, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));
        LocalDateTime searchFromTime = LocalDateTime.of(2026, 3, 1, 0, 0, 0, 0);
        LocalDateTime searchToTime = LocalDateTime.of(2026, 3, 1, 6, 0, 0, 0);
        /* mock */
        when(roomRepository.findAll(any(Specification.class))).thenReturn(List.of(room));
        when(closureRepository.findByRoomIdAndOverlapping(eq(null), eq(searchFromTime), eq(searchToTime)))
                .thenReturn(List.of());
        when(reservationRepository.findByRoomIdAndOverlappingAndActive(eq(null), eq(searchFromTime), eq(searchToTime)))
                .thenReturn(List.of());
        /* verify */
        List<RoomSchedule> result = roomService.searchAvailabilities(
                null, null, null, null, searchFromTime, searchToTime);
        assertThat(result).hasSize(0);
    }

    @Test
    void DeleteRoomSucceedTest() {
        Room room = new Room("101", 12, "Building A", null, null);
        Long searchId = 2L;
        List<Reservation> reservations = List.of(
                new Reservation(new User(), room,
                        LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0)),
                new Reservation(new User(), room,
                        LocalDateTime.of(2300, 3, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2300, 3, 1, 15, 30, 0, 0)));

        when(roomRepository.findById(eq((searchId)))).thenReturn(Optional.of(room));
        when(reservationRepository.findByRoomIdAndStartAfterAndActive(eq(searchId), any(LocalDateTime.class)))
                .thenReturn(reservations);
        doNothing().when(closureRepository).deleteByRoomIdAndAfterTime(eq(searchId), any(LocalDateTime.class));

        List<Reservation> closedReservations = roomService.deleteRoom(searchId);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.ROOM_STATUS_DELETED);
        assertThat(closedReservations).hasSize(2);
        assertThat(closedReservations)
                .usingRecursiveComparison()
                .ignoringFields("status")
                .isEqualTo(reservations);
        assertThat(closedReservations.get(0).getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
        assertThat(closedReservations.get(1).getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
    }

    @Test
    void DeleteRoomNotFoundTest() {
        Long searchId = 2L;
        when(roomRepository.findById(eq(searchId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> roomService.deleteRoom(searchId));
        assertThat(exception.getMessage()).isEqualTo("Room not found.");
    }
}
