package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.AddClosureResult;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.service.ClosureService;
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
        return closureRepository.findByRoomId(roomId);
    }

    /* TODO: constraints on time */
    @Override
    public AddClosureResult addClosure(Long roomId, Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        /* close reservations during closure */
        List<Reservation> reservations = reservationRepository.findByRoomIdAndOverlappingAndActive(roomId, startTime, endTime);
        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CLOSED);
        }
        /* merge existing closures that overlap */
        List<Closure> overlapping = closureRepository.findByRoomIdAndOverlappingOrAdjacent(roomId, startTime, endTime);
        LocalDateTime minStartTime = overlapping.stream().map(Closure::getStartTime).min(LocalDateTime::compareTo).orElse(startTime);
        LocalDateTime maxEndTime = overlapping.stream().map(Closure::getEndTime).max(LocalDateTime::compareTo).orElse(endTime);
        LocalDateTime mergedStartTime = minStartTime.isBefore(startTime) ? minStartTime : startTime;
        LocalDateTime mergedEndTime = maxEndTime.isAfter(endTime) ? maxEndTime : endTime;
        /* persist */
        closureRepository.deleteAll(overlapping);
        Closure closure = closureRepository.save(new Closure(room, mergedStartTime, mergedEndTime));
        return new AddClosureResult(closure, reservations);
    }

    @Override
    public void deleteClosure(Long closureId) {
        Closure closure = closureRepository.findById(closureId).orElseThrow(() -> new ResourceNotFoundException("Closure not found."));
        closureRepository.delete(closure);
    }
}
