package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.UserSpecifications;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND = "User not found.";
    private static final String LAST_ACTIVE_ADMIN_CANNOT_BE_REMOVED = "Last active admin cannot be removed.";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<User> searchUsers(String username, Role role, UserStatus status, Pageable pageable) {
        Pageable queryPageable = DefaultSorts.addUserDefaultSort(pageable);
        Specification<User> specification = Specification.unrestricted();
        if (username != null && !username.isBlank()) {
            specification = specification.and(UserSpecifications.usernameContains(username));
        }
        if (role != null) {
            specification = specification.and(UserSpecifications.hasRole(role));
        }
        if (status != null) {
            specification = specification.and(UserSpecifications.hasStatus(status));
        }
        return userRepository.findAll(specification, queryPageable);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    @Override
    public User addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateUsername(Long id, String username) {
        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        user.setUsername(username);
        return user;
    }

    @Override
    public User updatePassword(Long id, String password) {
        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }

    @Override
    public User addAdminRole(Long id) {
        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        user.addAdminRole();
        return user;
    }

    @Override
    public User removeAdminRole(Long id) {
        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        validateNotLastActiveAdmin(user);
        user.removeAdminRole();
        return user;
    }

    @Override
    public void closeUserAccount(Long id) {
        /* verify and acquire lock on user */
        User user = userRepository.findByIdWithLock(id).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (!user.isActive()) {
            return;
        }
        validateNotLastActiveAdmin(user);
        /* delete user info, but keep the record as FK of history reservations */
        user.setStatus(UserStatus.USER_STATUS_CLOSED);
        user.setUsername(null);
        user.setPassword(null);
        /* close reservations */
        List<Reservation> reservations = reservationRepository.findByUserIdAndStartAfterAndScheduled(
                id,
                LocalDateTime.now(),
                DefaultSorts.occupationSort());
        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RESERVATION_STATUS_CLOSED);
        }
    }

    private void validateNotLastActiveAdmin(User user) {
        if (!user.getRoles().contains(Role.ROLE_ADMIN)) {
            return;
        }
        if (!userRepository.existsByRolesContainsAndStatusAndIdNot(
                Role.ROLE_ADMIN,
                UserStatus.USER_STATUS_ACTIVE,
                user.getId())) {
            throw new IllegalStateException(LAST_ACTIVE_ADMIN_CANNOT_BE_REMOVED);
        }
    }
}
