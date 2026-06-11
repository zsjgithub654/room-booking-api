package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<User> searchUsers(String username, Role role, UserStatus status, Pageable pageable) {
        return userRepository.findByUsernameAndRoleAndStatus(username, role, status, pageable);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    @Override
    public User addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateUsername(Long id, String username) {
        User user = userRepository.findById(id)
                .filter(foundUser -> foundUser.getStatus() == UserStatus.USER_STATUS_ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setUsername(username);
        return user;
    }

    @Override
    public User updatePassword(Long id, String password) {
        User user = userRepository.findById(id)
                .filter(foundUser -> foundUser.getStatus() == UserStatus.USER_STATUS_ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }

    @Override
    public User addAdminRole(Long id) {
        User user = userRepository.findById(id)
                .filter(foundUser -> foundUser.getStatus() == UserStatus.USER_STATUS_ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.addAdminRole();
        return user;
    }

    @Override
    public User removeAdminRole(Long id) {
        User user = userRepository.findById(id)
                .filter(foundUser -> foundUser.getStatus() == UserStatus.USER_STATUS_ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.removeAdminRole();
        return user;
    }

    @Override
    public void closeUserAccount(Long id) {
        /* verify and acquire lock on user */
        User user = userRepository.findByIdWithLock(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (user.getStatus() == UserStatus.USER_STATUS_CLOSED) {
            return;
        }
        /* delete user info, but keep the record as FK of history reservations */
        user.setStatus(UserStatus.USER_STATUS_CLOSED);
        user.setUsername(null);
        user.setPassword(null);
        /* close reservations */
        List<Reservation> reservations = reservationRepository.findByUserIdAndStartAfterAndActive(id, LocalDateTime.now());
        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CLOSED);
        }
    }
}
