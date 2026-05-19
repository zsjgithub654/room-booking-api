package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public List<User> searchUsers() {
        return null;
    }

    @Override
    public User getUser(Long id) {
        return null;
    }

    @Override
    public User addUser(User user) {
        return null;
    }

    @Override
    public User updateUser(Long id, String username, String password) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        /* close reservations */
        List<Reservation> reservations = reservationRepository.findByRoomIdAndStartAfterAndActive(id, LocalDateTime.now());
        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CLOSED);
            reservation.setUser(null);
        }
        /* delete user */
        /* TODO: how to delete or inactivate an account */
        userRepository.deleteById(id);
    }

}