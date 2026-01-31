package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.entity.Reservation;
import com.zsj.RoomBooking.entity.Room;
import com.zsj.RoomBooking.entity.User;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation addReservation(long userId, long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        User user = userRepository.getReferenceById(userId);
        Room room = roomRepository.getReferenceById(roomId);
        reservationRepository.save(new Reservation(user, room, startTime, endTime));
        return null;
    }

    @Override
    public Reservation cancelReservation(long reservationId) {
        return null;
    }

    @Override
    public Reservation modifyReservationTime(long reservationId, LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }
}
