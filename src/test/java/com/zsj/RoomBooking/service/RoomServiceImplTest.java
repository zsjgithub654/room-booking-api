package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.Availability;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.RoomStatus;
import com.zsj.RoomBooking.model.TimeRange;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        Room room = new Room("101", 12, "Building A");
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
        Room room = new Room("101", 12, "Building A");
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        assertThat(roomService.addRoom(room))
                .usingRecursiveComparison()
                .isEqualTo(room);
    }

    @Test
    void UpdateRoomSucceedTest() {
        Room room = new Room("101", 12, "Building A");
        Room newRoom = new Room("102", 10, "Building 1");
        Long searchId = 2L;
        when(roomRepository.findById(eq(searchId))).thenReturn(Optional.of(room));
        assertThat(roomService.updateRoom(searchId, newRoom.getName(), newRoom.getCapacity(), newRoom.getArea()))
                .usingRecursiveComparison()
                .isEqualTo(newRoom);
    }

    @Test
    void UpdateRoomNotFoundTest() {
        Room newRoom = new Room("102", 10, "Building 1");
        Long searchId = 2L;

        when(roomRepository.findById(eq(searchId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class,
                () -> roomService.updateRoom(searchId, newRoom.getName(), newRoom.getCapacity(), newRoom.getArea()));
        assertThat(exception.getMessage()).isEqualTo("Room not found.");
    }

    @Test
    void SearchRoomsTest() {
        List<Room> rooms = List.of(
                new Room("101", 12, "Building A"),
                new Room("102", 4, "Building A"),
                new Room("101", 6, "Building B")
        );
        when(roomRepository.findAll(any(Specification.class))).thenReturn(rooms);
        List<Room> result = roomService.searchRooms(null, null, null, null);
        assertThat(result).hasSize(3);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(rooms);
    }

    @Test
    void searchAvailabilitiesTest() {
        Room room = new Room("101", 12, "Building A");
        List<TimeRange> reservations = List.of(
                new TimeRange(
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0)),
                new TimeRange(
                        LocalDateTime.of(2026, 3, 1, 16, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 17, 0, 0, 0)));
        List<TimeRange> closures = List.of(
                new TimeRange(
                        LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 16, 0, 0, 0)));
        /* mock */
        when(roomRepository.findAll(any(Specification.class))).thenReturn(List.of(room));
        when(closureRepository.getTimeByRoomIdAndInterval(eq(null), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(closures);
        when(reservationRepository.getTimeByRoomIdAndIntervalAndActive(eq(null), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(reservations);
        /* verify */
        /* search range start and end time don't overlapped with occupations in between */
        List<Availability> result = roomService.searchAvailabilities(null, null, null, null,
                LocalDateTime.of(2026, 3, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 3, 2, 0, 0, 0, 0));
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(new Availability(room, List.of(
                        new TimeRange(
                                LocalDateTime.of(2026, 3, 1, 0, 0, 0, 0),
                                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0)),
                        new TimeRange(
                                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0),
                                LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0)),
                        new TimeRange(
                                LocalDateTime.of(2026, 3, 1, 17, 0, 0, 0),
                                LocalDateTime.of(2026, 3, 2, 0, 0, 0, 0))
                )));
        /* search range start time overlap with occupation */
        result = roomService.searchAvailabilities(null, null, null, null,
                LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0),
                LocalDateTime.of(2026, 3, 2, 0, 0, 0, 0));
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(new Availability(room, List.of(
                        new TimeRange(
                                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0),
                                LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0)),
                        new TimeRange(
                                LocalDateTime.of(2026, 3, 1, 17, 0, 0, 0),
                                LocalDateTime.of(2026, 3, 2, 0, 0, 0, 0))
                )));
        /* search range end time overlap with occupation */
        result = roomService.searchAvailabilities(null, null, null, null,
                LocalDateTime.of(2026, 3, 1, 0, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 14, 0, 0, 0));
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(new Availability(room, List.of(
                        new TimeRange(
                                LocalDateTime.of(2026, 3, 1, 0, 0, 0, 0),
                                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0)),
                        new TimeRange(
                                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0),
                                LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0))
                )));
    }

    @Test
    void DeleteRoomSucceedTest() {
        Room room = new Room("101", 12, "Building A");
        Long searchId = 2L;
        List<Reservation> reservations = List.of(
                new Reservation(new User(), room,
                        LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0)),
                new Reservation(new User(), room,
                        LocalDateTime.of(2300, 3, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2300, 3, 1, 15, 30, 0, 0)));

        when(roomRepository.findById(eq((searchId)))).thenReturn(Optional.of(room));
        when(reservationRepository.findByRoomIdAndStartAfterAndActive(eq(searchId),any(LocalDateTime.class)))
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
