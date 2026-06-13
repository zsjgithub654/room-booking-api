package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.result.AddClosureResult;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.service.ClosureService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
public class ClosureServiceImpl implements ClosureService {
    @Autowired
    ClosureRepository closureRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Override
    public Closure getClosure(Long id) {
        return closureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Closure not found."));
    }

    @Override
    public List<Closure> getClosuresOfRoom(Long roomId) {
        return closureRepository.findByRoomId(roomId, DefaultSorts.occupationSort());
    }

    @Override
    public AddClosureResult addClosure(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        /* check room and acquire lock */
        Room room = roomRepository.findByIdWithLock(roomId)
                .filter(Room::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        /* close reservations during closure */
        List<Reservation> reservations = reservationRepository.findByRoomIdAndOverlappingAndScheduled(
                roomId, startTime, endTime, DefaultSorts.occupationSort());
        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CLOSED);
        }
        /* add closure and merge with existing closures that overlap */
        Closure closure = addClosureAndMerge(roomId, startTime, endTime, room);
        return new AddClosureResult(closure, reservations);
    }

    private @NonNull Closure addClosureAndMerge(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Room room) {
        List<Closure> overlapping = closureRepository.findByRoomIdAndOverlappingOrAdjacent(roomId, startTime, endTime);
        LocalDateTime minStartTime = overlapping.stream().map(Closure::getStartTime).min(LocalDateTime::compareTo).orElse(startTime);
        LocalDateTime maxEndTime = overlapping.stream().map(Closure::getEndTime).max(LocalDateTime::compareTo).orElse(endTime);
        /* persist */
        closureRepository.deleteAll(overlapping);
        return closureRepository.save(new Closure(room,
                minStartTime.isBefore(startTime) ? minStartTime : startTime,
                maxEndTime.isAfter(endTime) ? maxEndTime : endTime));
    }

    @Override
    public void deleteClosure(Long closureId) {
        Closure closure = closureRepository.findById(closureId).orElseThrow(() -> new ResourceNotFoundException("Closure not found."));
        closureRepository.delete(closure);
    }
}
