package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.AddClosureResult;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.service.impl.ClosureServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

@ExtendWith(MockitoExtension.class)
public class ClosureServiceImplTest {
    @Mock
    private ClosureRepository closureRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ClosureServiceImpl closureService;

    @Test
    void GetClosureSucceedTest() {
        Closure closure = new Closure(new Room(),
                LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0));
        Long searchId = 2L;
        when(closureRepository.findById(eq(searchId))).thenReturn(Optional.of(closure));

        assertThat(closureService.getClosure(searchId))
                .usingRecursiveComparison()
                .isEqualTo(closure);
    }

    @Test
    void GetClosureNotFoundTest() {
        Long searchId = 2L;
        when(closureRepository.findById(eq(searchId))).thenThrow(new ResourceNotFoundException("Closure not found."));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> closureService.getClosure(searchId));
        assertThat(exception.getMessage()).isEqualTo("Closure not found.");
    }

    @Test
    void GetClosuresOfRoomSucceedTest() {
        Long searchRoomId = 2L;
        List<Closure> closures = List.of(
                new Closure(new Room(),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0)),
                new Closure(new Room(),
                        LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0)));
        when(closureRepository.findByRoomId(eq(searchRoomId))).thenReturn(closures);

        List<Closure> result = closureService.getClosuresOfRoom(searchRoomId);
        assertThat(result).hasSize(closures.size());
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(closures);
    }

    @Test
    void GetClosuresOfRoomNoResultTest() {
        Long roomId = 2L;
        when(closureRepository.findByRoomId(eq(roomId))).thenReturn(List.of());

        assertThat(closureService.getClosuresOfRoom(roomId)).hasSize(0);
    }

    @Test
    void AddClosureSucceedReservationsClosedTest() {
        Room room = new Room("101", 12, "Building A", null, null);
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 16, 0, 0, 0);
        List<Reservation> reservations = List.of(
                new Reservation(new User(), room,
                        LocalDateTime.of(2300, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2300, 3, 1, 13, 0, 0, 0)),
                new Reservation(new User(), room,
                        LocalDateTime.of(2300, 3, 1, 14, 0, 0, 0),
                        LocalDateTime.of(2300, 3, 1, 15, 0, 0, 0)));
        List<Closure> overlappingClosures = List.of(
                new Closure(new Room(),
                        LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 14, 0, 0, 0)),
                new Closure(new Room(),
                        LocalDateTime.of(2026, 3, 1, 16, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 17, 0, 0, 0)));
        Long roomId = 2L;
        /* mock */
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(reservationRepository.findByRoomIdAndOverlappingAndActive(roomId, startTime, endTime))
                .thenReturn(reservations);
        when(closureRepository.findByRoomIdAndOverlappingOrAdjacent(roomId, startTime, endTime))
                .thenReturn(overlappingClosures);
        doAnswer(returnsFirstArg()).when(closureRepository).save(any(Closure.class));
        doNothing().when(closureRepository).deleteAll(eq(overlappingClosures));
        /* verify */
        AddClosureResult result = closureService.addClosure(roomId, startTime, endTime);
        assertThat(result.getClosure().getStartTime()).isEqualTo(startTime);
        assertThat(result.getClosure().getEndTime()).isEqualTo(LocalDateTime.of(2026, 3, 1, 17, 0, 0, 0));
        assertThat(result.getCanceledReservations()).hasSize(reservations.size());
        assertThat(result.getCanceledReservations())
                .usingRecursiveComparison()
                .isEqualTo(reservations);
    }

    @Test
    void AddClosureRoomNotFoundTest() {
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 16, 0, 0, 0);
        Long roomId = 2L;
        /* mock */
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.empty());
        /* verify */
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> closureService.addClosure(roomId, startTime, endTime));
        assertThat(exception.getMessage()).isEqualTo("Room not found.");
    }

    @Test
    void DeleteClosureTest() {
        Closure closure = new Closure(new Room(),
                LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0),
                LocalDateTime.of(2026, 3, 1, 14, 0, 0, 0));
        Long closureId = 2L;

        when(closureRepository.findById(closureId)).thenReturn(Optional.of(closure));
        doNothing().when(closureRepository).delete(eq(closure));

        closureService.deleteClosure(closureId);
    }

    @Test
    void DeleteClosureNotFoundTest() {
        Long searchId = 2L;

        when(closureRepository.findById(eq(searchId))).thenThrow(new ResourceNotFoundException("Closure not found."));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> closureService.deleteClosure(searchId));
        assertThat(exception.getMessage()).isEqualTo("Closure not found.");
    }
}
