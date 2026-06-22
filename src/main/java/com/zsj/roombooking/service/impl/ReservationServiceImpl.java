package com.zsj.roombooking.service.impl;

import com.zsj.roombooking.exception.ResourceNotFoundException;
import com.zsj.roombooking.model.ReservationStatus;
import com.zsj.roombooking.model.entity.Reservation;
import com.zsj.roombooking.model.entity.Room;
import com.zsj.roombooking.model.entity.User;
import com.zsj.roombooking.repository.ClosureRepository;
import com.zsj.roombooking.repository.ReservationRepository;
import com.zsj.roombooking.repository.ReservationSpecifications;
import com.zsj.roombooking.repository.RoomRepository;
import com.zsj.roombooking.repository.UserRepository;
import com.zsj.roombooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Transactional
@Service
public class ReservationServiceImpl implements ReservationService {
    private static final String RESERVATION_NOT_FOUND = "Reservation not found.";
    private static final String CANCELED_RESERVATION_CANNOT_BE_UPDATED = "Cannot update a canceled reservation.";
    private static final String CLOSED_RESERVATION_CANNOT_BE_UPDATED = "Cannot update a closed reservation.";
    private static final String USER_NOT_FOUND = "User not found.";
    private static final String CLOSED_USER_CANNOT_BOOK_RESERVATIONS = "Cannot book reservations for a closed user account.";
    private static final String ROOM_NOT_FOUND = "Room not found.";
    private static final String STARTED_RESERVATION_CANNOT_BE_UPDATED = "Started reservation cannot be updated.";
    private static final String ROOM_NOT_IN_OPEN_HOURS = "Room not in open hours.";
    private static final String ROOM_IN_CLOSURE = "Room is in closure during selected time.";
    private static final String ROOM_RESERVED = "Room is reserved in selected time.";

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
        Specification<Reservation> specification = Specification.unrestricted();
        if (userId != null) {
            specification = specification.and(ReservationSpecifications.hasUserId(userId));
        }
        if (roomId != null) {
            specification = specification.and(ReservationSpecifications.hasRoomId(roomId));
        }
        if (date != null) {
            specification = specification.and(ReservationSpecifications.startsAtOrAfter(date.atStartOfDay()));
            specification = specification.and(ReservationSpecifications.startsBefore(date.plusDays(1).atStartOfDay()));
        }
        if (status != null) {
            specification = specification.and(ReservationSpecifications.hasStatus(status));
        }
        return reservationRepository.findAll(specification, queryPageable);
    }

    @Override
    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(RESERVATION_NOT_FOUND));
    }

    @Override
    public Reservation addReservation(Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        /* verify and acquire lock on user and room, keep order of acquiring locks consistent across transactions */
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new IllegalStateException(CLOSED_USER_CANNOT_BOOK_RESERVATIONS);
        }
        Room room = roomRepository.findByIdWithLock(roomId)
                .filter(Room::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));
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
                .orElseThrow(() -> new ResourceNotFoundException(RESERVATION_NOT_FOUND));
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
                .orElseThrow(() -> new ResourceNotFoundException(RESERVATION_NOT_FOUND));
        if (reservation.getStatus() == ReservationStatus.RESERVATION_STATUS_CANCELED) {
            throw new IllegalStateException(CANCELED_RESERVATION_CANNOT_BE_UPDATED);
        }
        if (reservation.getStatus() == ReservationStatus.RESERVATION_STATUS_CLOSED) {
            throw new IllegalStateException(CLOSED_RESERVATION_CANNOT_BE_UPDATED);
        }
        if (!reservation.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(STARTED_RESERVATION_CANNOT_BE_UPDATED);
        }
        /* acquire lock on user and room */
        User user = userRepository.findByIdWithLock(reservation.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new IllegalStateException(CLOSED_USER_CANNOT_BOOK_RESERVATIONS);
        }
        Room room = roomRepository.findByIdWithLock(reservation.getRoom().getId())
                .filter(Room::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));
        /* check availability if not within original range */
        if (startTime.isBefore(reservation.getStartTime())
                || endTime.isAfter(reservation.getEndTime())) {
            validateReservationWithinOpenHours(room, startTime, endTime);
            validateNoOverlappingClosure(room.getId(), startTime, endTime);
            validateNoOverlappingReservation(room.getId(), reservation.getId(), startTime, endTime);
        }
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
            throw new IllegalStateException(ROOM_NOT_IN_OPEN_HOURS);
        }
    }

    private void validateNoOverlappingClosure(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        if (closureRepository.existsByRoomIdAndOverlapping(roomId, startTime, endTime)) {
            throw new IllegalStateException(ROOM_IN_CLOSURE);
        }
    }

    private void validateNoOverlappingReservation(Long roomId, Long currentReservation, LocalDateTime startTime, LocalDateTime endTime) {
        boolean hasOverlap = currentReservation == null
                ? reservationRepository.existsByRoomIdAndOverlappingAndScheduled(roomId, startTime, endTime)
                : reservationRepository.existsByRoomIdAndOverlappingAndScheduledExcludingReservation(
                        roomId, currentReservation, startTime, endTime);
        if (hasOverlap) {
            throw new IllegalStateException(ROOM_RESERVED);
        }
    }

    private LocalDateTime getReleasedEndTime(LocalDateTime currentTime) {
        if (currentTime.getSecond() == 0 && currentTime.getNano() == 0) {
            return currentTime;
        }
        return currentTime.plusMinutes(1).withSecond(0).withNano(0);
    }
}
