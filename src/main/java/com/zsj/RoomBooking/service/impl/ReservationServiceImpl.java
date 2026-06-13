package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ClosureRepository;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    public Page<Reservation> searchReservations(Long userId, Long roomId, LocalDate date, ReservationStatus status, Pageable pageable) {
        Pageable queryPageable = DefaultSorts.addReservationDefaultSort(pageable);
        return reservationRepository.findByUserIdAndRoomIdAndStartTimeAndStatus(
                userId, roomId,
                date == null ? null : date.atStartOfDay(),
                date == null ? null : date.plusDays(1).atStartOfDay(),
                status, queryPageable);
    }

    @Override
    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));
    }

    @Override
    public Reservation addReservation(Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        /* verify and acquire lock on user and room, keep order of acquiring locks consistent across transactions */
        User user = userRepository.findByIdWithLock(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        Room room = roomRepository.findByIdWithLock(roomId)
                .filter(Room::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        /* check availability */
        validateReservationWithinOpenHours(room, startTime, endTime);
        validateNoOverlappingClosure(room.getId(), startTime, endTime);
        validateNoOverlappingReservation(room.getId(), null, startTime, endTime);
        /* add reservation */
        return reservationRepository.save(new Reservation(user, room, startTime, endTime));
    }

    @Override
    public void releaseReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));
        if (!reservation.isScheduled()) {
            return;
        }
        /* passed */
        LocalDateTime currentTime = LocalDateTime.now();
        if (!reservation.getEndTime().isAfter(currentTime)) {
            return;
        }
        /* started but not ended */
        if (!reservation.getStartTime().isAfter(currentTime)) {
            reservation.setTime(reservation.getStartTime(), getReleasedEndTime(currentTime));
            return;
        }
        /* haven't started yet */
        reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CANCELED);
    }

    @Override
    public Reservation updateReservationTime(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        Reservation reservation = reservationRepository.findById(id)
                .filter(Reservation::isScheduled)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found."));
        if (!reservation.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Started reservation cannot be updated.");
        }
        /* acquire lock on user and room */
        userRepository.findByIdWithLock(reservation.getUser().getId())
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        Room room = roomRepository.findByIdWithLock(reservation.getRoom().getId())
                .filter(Room::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found."));
        /* check availability */
        validateReservationWithinOpenHours(room, startTime, endTime);
        validateNoOverlappingClosure(room.getId(), startTime, endTime);
        validateNoOverlappingReservation(room.getId(), reservation.getId(), startTime, endTime);
        /* update reservation */
        reservation.setTime(startTime, endTime);
        return reservation;
    }

    private void validateReservationWithinOpenHours(Room room, LocalDateTime startTime, LocalDateTime endTime) {
        if (room.isOpenAllDay()) {
            return;
        }
        /* cross day, or outside open hours */
        if (!startTime.toLocalDate().equals(endTime.toLocalDate())
                || startTime.toLocalTime().isBefore(room.getOpenTime())
                || endTime.toLocalTime().isAfter(room.getCloseTime())) {
            throw new IllegalStateException("Room is not in open hours during selected time.");
        }
    }

    private void validateNoOverlappingClosure(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        if (closureRepository.existsByRoomIdAndOverlapping(roomId, startTime, endTime)) {
            throw new IllegalStateException("Room is in closure during selected time.");
        }
    }

    private void validateNoOverlappingReservation(Long roomId, Long currentReservation, LocalDateTime startTime, LocalDateTime endTime) {
        boolean hasOverlap = currentReservation == null
                ? reservationRepository.existsByRoomIdAndOverlappingAndScheduled(roomId, startTime, endTime)
                : reservationRepository.existsByRoomIdAndOverlappingAndScheduledExcludingReservation(
                        roomId, currentReservation, startTime, endTime);
        if (hasOverlap) {
            throw new IllegalStateException("Room is reserved in selected time.");
        }
    }

    private LocalDateTime getReleasedEndTime(LocalDateTime currentTime) {
        if (currentTime.getSecond() == 0 && currentTime.getNano() == 0) {
            return currentTime;
        }
        return currentTime.plusMinutes(1).withSecond(0).withNano(0);
    }
}
