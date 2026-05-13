package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<Reservation> searchReservations(Long userId, Long roomId, LocalDate date, ReservationStatus status) {
        return null;
    }

    @Override
    public Reservation getReservation(Long id) {
        return reservationRepository.getReferenceById(id);
    }

    @Override
    public Reservation addReservation(Long userId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }

    @Override
    public void deleteReservation(Long reservationId) {
    }

    @Override
    public Reservation updateReservationTime(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }
}
