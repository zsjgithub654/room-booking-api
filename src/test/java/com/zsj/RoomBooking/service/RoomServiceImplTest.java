package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.Occupation;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.RoomStatus;
import com.zsj.RoomBooking.model.criteria.RoomSearchCriteria;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.model.result.RoomSchedule;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
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
import static org.mockito.Mockito.verify;
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
        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoom(searchId));
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
    void AddRoomInvalidOpenHoursTest() {
        assertThrows(IllegalArgumentException.class,
                () -> new Room("101", 12, "Building A",
                        LocalTime.of(16, 0, 0, 0),
                        LocalTime.of(9, 0, 0, 0)));
    }

    @Test
    void UpdateRoomSucceedTest() {
        Room room = new Room("101", 12, "Building A", null, null);
        Room newRoom = new Room("102", 10, "Building 1",
                LocalTime.of(8, 30, 0, 0),
                LocalTime.of(16, 30, 0, 0));
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
    void UpdateRoomCanSetBackToOpenAllDayTest() {
        Room room = new Room("101", 12, "Building A",
                LocalTime.of(8, 30, 0, 0),
                LocalTime.of(16, 30, 0, 0));
        Long searchId = 2L;
        when(roomRepository.findById(eq(searchId))).thenReturn(Optional.of(room));

        Room result = roomService.updateRoom(searchId,
                room.getName(),
                room.getCapacity(),
                room.getArea(),
                null,
                null);

        assertThat(result.isOpenAllDay()).isTrue();
    }

    @Test
    void UpdateRoomNotFoundTest() {
        Room newRoom = new Room("102", 10, "Building 1", null, null);
        Long searchId = 2L;

        when(roomRepository.findById(eq(searchId))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.updateRoom(searchId,
                        newRoom.getName(),
                        newRoom.getCapacity(),
                        newRoom.getArea(),
                        newRoom.getOpenTime(),
                        newRoom.getCloseTime()));
    }

    @Test
    void SearchRoomsTest() {
        RoomSearchCriteria criteria = new RoomSearchCriteria(null, null, null, null, null);
        List<Room> rooms = List.of(
                new Room("101", 12, "Building A", null, null),
                new Room("102", 4, "Building A", null, null),
                new Room("101", 6, "Building B", null, null)
        );
        when(roomRepository.findAll(any(Specification.class), eq(Pageable.unpaged(getRoomSort()))))
                .thenReturn(new PageImpl<>(rooms));
        List<Room> result = roomService.searchRooms(criteria, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(3);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(rooms);
    }

    @Test
    void SearchRoomsShouldApplyDefaultSortToPagedQueryTest() {
        RoomSearchCriteria criteria = new RoomSearchCriteria(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20);
        Pageable expectedPageable = PageRequest.of(0, 20, getRoomSort());

        when(roomRepository.findAll(any(Specification.class), eq(expectedPageable)))
                .thenReturn(new PageImpl<>(List.of(), expectedPageable, 0));

        roomService.searchRooms(criteria, pageable);

        verify(roomRepository).findAll(any(Specification.class), eq(expectedPageable));
    }

    @Test
    void searchAvailabilitiesHasResultTest() {
        Room room = new Room("101", 12, "Building A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));
        List<Reservation> reservations = List.of(
                new Reservation(new User(), room,
                        LocalDateTime.of(2026, 3, 1, 8, 30, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0)));
        List<Closure> closures = List.of(
                new Closure(room,
                        LocalDateTime.of(2026, 3, 3, 0, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 3, 16, 30, 0, 0)));
        LocalDate searchStartDate = LocalDate.of(2026, 3, 1);
        LocalDate searchEndDate = LocalDate.of(2026, 3, 3);
        /* mock */
        when(roomRepository.findAll(any(Specification.class), eq(Pageable.unpaged(getRoomSort()))))
                .thenReturn(new PageImpl<>(List.of(room)));
        when(closureRepository.findByRoomIdAndOverlapping(eq(null),
                eq(searchStartDate.atStartOfDay()),
                eq(searchEndDate.plusDays(1).atStartOfDay())))
                .thenReturn(closures);
        when(reservationRepository.findByRoomIdAndOverlappingAndScheduled(eq(null),
                eq(searchStartDate.atStartOfDay()),
                eq(searchEndDate.plusDays(1).atStartOfDay()),
                eq(getOccupationSort())))
                .thenReturn(reservations);
        /* verify */
        List<RoomSchedule> result = roomService.searchAvailabilities(
                null, null, null, null, searchStartDate, searchEndDate, false);
        assertThat(result).hasSize(1);
        List<Occupation> expectedOccupations = new ArrayList<>();
        expectedOccupations.addAll(reservations);
        expectedOccupations.addAll(closures);
        assertThat(result.get(0).room())
                .usingRecursiveComparison()
                .isEqualTo(room);
        assertThat(result.get(0).occupations()).hasSize(reservations.size() + closures.size());
        assertThat(result.get(0).occupations())
                .usingRecursiveComparison()
                .isEqualTo(expectedOccupations);
    }
    @Test
    void searchAvailabilitiesCanIncludeUnavailableRoomsTest() {
        /* full */
        Room room = new Room("101", 12, "Building A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));
        List<Reservation> reservations = List.of(
                new Reservation(new User(), room,
                        LocalDateTime.of(2026, 3, 1, 9, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 14, 0, 0, 0)));
        List<Closure> closures = List.of(
                new Closure(room,
                        LocalDateTime.of(2026, 3, 1, 14, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 16, 0, 0, 0)));
        LocalDate searchStartDate = LocalDate.of(2026, 3, 1);
        LocalDate searchEndDate = LocalDate.of(2026, 3, 1);
        /* mock */
        when(roomRepository.findAll(any(Specification.class), eq(Pageable.unpaged(getRoomSort()))))
                .thenReturn(new PageImpl<>(List.of(room)));
        when(closureRepository.findByRoomIdAndOverlapping(eq(null),
                eq(searchStartDate.atStartOfDay()),
                eq(searchEndDate.plusDays(1).atStartOfDay())))
                .thenReturn(closures);
        when(reservationRepository.findByRoomIdAndOverlappingAndScheduled(eq(null),
                eq(searchStartDate.atStartOfDay()),
                eq(searchEndDate.plusDays(1).atStartOfDay()),
                eq(getOccupationSort())))
                .thenReturn(reservations);
        /* verify */
        /* include unavailable rooms */
        List<RoomSchedule> result = roomService.searchAvailabilities(
                null, null, null, null, searchStartDate, searchEndDate, true);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).room())
                .usingRecursiveComparison()
                .isEqualTo(room);
        assertThat(result.get(0).occupations())
                .usingRecursiveComparison()
                .isEqualTo(List.of(
                        reservations.get(0),
                        closures.get(0)
                ));
        /* exclude unavailable rooms */
        assertThat(roomService.searchAvailabilities(
                null, null, null, null,
                searchStartDate, searchEndDate,
                false))
                .hasSize(0);
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

        when(roomRepository.findByIdWithLock(eq(searchId))).thenReturn(Optional.of(room));
        when(reservationRepository.findByRoomIdAndStartAfterAndScheduled(
                eq(searchId), any(LocalDateTime.class), eq(getOccupationSort())))
                .thenReturn(reservations);
        doNothing().when(closureRepository).deleteByRoomIdAndStartAfter(eq(searchId), any(LocalDateTime.class));

        List<Reservation> closedReservations = roomService.deleteRoom(searchId);
        org.mockito.Mockito.verify(roomRepository).findByIdWithLock(searchId);
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
    void DeleteRoomShouldSortClosedReservationsByTimeTest() {
        Room room = new Room("101", 12, "Building A", null, null);
        Long searchId = 2L;
        Reservation earlierReservation = new Reservation(new User(), room,
                LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0));
        Reservation laterReservation = new Reservation(new User(), room,
                LocalDateTime.of(2300, 3, 1, 14, 30, 0, 0),
                LocalDateTime.of(2300, 3, 1, 15, 30, 0, 0));

        when(roomRepository.findByIdWithLock(eq(searchId))).thenReturn(Optional.of(room));
        when(reservationRepository.findByRoomIdAndStartAfterAndScheduled(
                eq(searchId), any(LocalDateTime.class), eq(getOccupationSort())))
                .thenReturn(List.of(earlierReservation, laterReservation));
        doNothing().when(closureRepository).deleteByRoomIdAndStartAfter(eq(searchId), any(LocalDateTime.class));

        List<Reservation> closedReservations = roomService.deleteRoom(searchId);

        assertThat(closedReservations)
                .usingRecursiveComparison()
                .ignoringFields("status")
                .isEqualTo(List.of(earlierReservation, laterReservation));
        assertThat(closedReservations.get(0).getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
        assertThat(closedReservations.get(1).getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
    }

    @Test
    void DeleteRoomAlreadyDeletedTest() {
        Room room = new Room("101", 12, "Building A", null, null);
        room.setStatus(RoomStatus.ROOM_STATUS_DELETED);
        Long searchId = 2L;

        when(roomRepository.findByIdWithLock(eq(searchId))).thenReturn(Optional.of(room));

        assertThat(roomService.deleteRoom(searchId)).isEmpty();
    }

    @Test
    void DeleteRoomNotFoundTest() {
        Long searchId = 2L;
        when(roomRepository.findByIdWithLock(eq(searchId))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.deleteRoom(searchId));
    }

    private Sort getRoomSort() {
        return Sort.by(
                Sort.Order.asc("id"),
                Sort.Order.asc("name"));
    }

    private Sort getOccupationSort() {
        return Sort.by(
                Sort.Order.asc("startTime"),
                Sort.Order.asc("endTime"),
                Sort.Order.asc("id"));
    }
}
