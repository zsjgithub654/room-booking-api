package com.zsj.RoomBooking.service.impl;

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
import com.zsj.RoomBooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ClosureRepository closureRepository;

    @Override
    public List<Reservation> searchReservations(Long userId, Long roomId, LocalDate date, ReservationStatus status) {
        return reservationRepository.findByUserIdAndRoomIdAndDateAndStatus(userId, roomId, date, status);
    }

    @Override
    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));
    }

    @Override
    public Reservation addReservation(Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        /* verify and acquire lock on user and room, keep order of acquiring locks consistent across transactions */
        User user = userRepository.findByIdWithLock(userId)
                .filter(foundUser -> foundUser.getStatus() == UserStatus.USER_STATUS_ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        Room room = roomRepository.findByIdWithLock(roomId)
                .filter(foundRoom -> foundRoom.getStatus() == RoomStatus.ROOM_STATUS_ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        /* check existing closures and reservations */
        if (closureRepository.existsByRoomIdAndOverlapping(roomId, startTime, endTime)
                || reservationRepository.existsByRoomIdAndOverlappingAndActive(roomId, startTime, endTime)) {
            throw new IllegalStateException("Room is not available in selected time.");
        }
        /* add reservation */
        return reservationRepository.save(new Reservation(user, room, startTime, endTime));
    }

    /* TODO: actually cancel */
    @Override
    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));
        reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED);
    }

    @Override
    public Reservation updateReservationTime(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        Reservation reservation = reservationRepository.findById(id)
                .filter(foundReservation -> foundReservation.getStatus() == ReservationStatus.RESERVATION_STATUS_ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));
        /* acquire lock on user and room */
        User user = userRepository.findByIdWithLock(reservation.getUser().getId()).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        Room room = roomRepository.findByIdWithLock(reservation.getRoom().getId()).orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        /* check availability */
        if (closureRepository.existsByRoomIdAndOverlapping(room.getId(), startTime, endTime)
                || reservationRepository.existsByRoomIdAndOverlappingAndActive(room.getId(), startTime, endTime)) {
            throw new IllegalStateException("Room is not available in selected time.");
        }
        /* update reservation */
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        return reservation;
    }
}
