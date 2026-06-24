package com.zsj.roombooking.service.impl;

import com.zsj.roombooking.exception.ResourceNotFoundException;
import com.zsj.roombooking.model.result.AddClosureResult;
import com.zsj.roombooking.model.ReservationStatus;
import com.zsj.roombooking.model.entity.Closure;
import com.zsj.roombooking.model.entity.Reservation;
import com.zsj.roombooking.model.entity.Room;
import com.zsj.roombooking.repository.ClosureRepository;
import com.zsj.roombooking.repository.ReservationRepository;
import com.zsj.roombooking.repository.RoomRepository;
import com.zsj.roombooking.service.ClosureService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class ClosureServiceImpl implements ClosureService {
    private static final String CLOSURE_NOT_FOUND = "Closure not found.";
    private static final String PASSED_CLOSURE_CANNOT_BE_DELETED = "Cannot delete a passed closure.";
    private static final String ROOM_NOT_FOUND = "Room not found.";

    @Autowired
    ClosureRepository closureRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Override
    public Closure getClosure(Long id) {
        return closureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(CLOSURE_NOT_FOUND));
    }

    @Override
    public List<Closure> getClosuresOfRoom(Long roomId) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));
        return closureRepository.findByRoomId(roomId, DefaultSorts.occupationSort());
    }

    @Override
    public AddClosureResult addClosure(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        /* check room and acquire lock */
        Room room = roomRepository.findByIdWithLock(roomId)
                .filter(Room::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));
        /* close reservations during closure */
        List<Reservation> reservations = reservationRepository.findByRoomIdAndOverlappingAndScheduled(
                roomId, startTime, endTime, DefaultSorts.occupationSort());
        for (Reservation reservation : reservations) {
            /* for started reservation, allow it to last until closure */
            if (reservation.getStartTime().isBefore(LocalDateTime.now())) {
                reservation.setTime(reservation.getStartTime(), startTime);
                continue;
            }
            reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CLOSED);
        }
        /* add closure and merge with existing closures that overlap */
        Closure closure = addClosureAndMerge(roomId, startTime, endTime, room);
        return new AddClosureResult(closure, reservations);
    }

    private Closure addClosureAndMerge(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Room room) {
        /* no overlapping closures */
        List<Closure> overlapping = closureRepository.findByRoomIdAndOverlappingOrAdjacent(roomId, startTime, endTime);
        if (overlapping.isEmpty()) {
            return closureRepository.save(new Closure(room, startTime, endTime));
        }
        /* merge to existing closure */
        LocalDateTime mergedStartTime = startTime;
        LocalDateTime mergedEndTime = endTime;
        for (Closure closure : overlapping) {
            if (closure.getStartTime().isBefore(mergedStartTime)) {
                mergedStartTime = closure.getStartTime();
            }
            if (closure.getEndTime().isAfter(mergedEndTime)) {
                mergedEndTime = closure.getEndTime();
            }
        }
        Closure keptClosure = overlapping.get(0);
        if (!keptClosure.getStartTime().equals(mergedStartTime)
                || !keptClosure.getEndTime().equals(mergedEndTime)) {
            keptClosure.setTime(mergedStartTime, mergedEndTime);
            closureRepository.save(keptClosure);
        }
        if (overlapping.size() > 1) {
            closureRepository.deleteAll(new ArrayList<>(overlapping.subList(1, overlapping.size())));
        }
        return keptClosure;
    }

    @Override
    public void deleteClosure(Long closureId) {
        Closure closure = closureRepository.findById(closureId).orElseThrow(() -> new ResourceNotFoundException(CLOSURE_NOT_FOUND));
        if (closure.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(PASSED_CLOSURE_CANNOT_BE_DELETED);
        }
        closureRepository.delete(closure);
    }
}
