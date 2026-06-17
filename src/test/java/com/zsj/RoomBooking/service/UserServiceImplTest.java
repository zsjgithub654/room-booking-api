package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.exception.ResourceNotFoundException;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/* to integrate Mockito */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void GetUserSucceedTest() {
        User user = new User("user1", "");
        Long searchId = 2L;
        when(userRepository.findById(eq(searchId))).thenReturn(Optional.of(user));
        assertThat(userService.getUser(searchId))
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void GetUserNotFoundTest() {
        Long searchId = 2L;
        when(userRepository.findById(eq(searchId))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(searchId));
    }

    @Test
    void AddUserTest() {
        User user = new User("user1", "");
        when(passwordEncoder.encode(eq(user.getPassword()))).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        assertThat(userService.addUser(user))
                .usingRecursiveComparison()
                .isEqualTo(user);
        assertThat(user.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void UpdateUsernameSucceedTest() {
        User user = new User("user1", "");
        Long searchId = 2L;
        String newName = "user1new";

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.of(user));
        User newUser = userService.updateUsername(searchId, newName);

        assertThat(newUser)
                .usingRecursiveComparison()
                .ignoringFields("username")
                .isEqualTo(user);
        assertThat(newUser.getUsername()).isEqualTo(newName);
    }

    @Test
    void UpdateUsernameNotFoundTest() {
        Long searchId = 2L;
        String newName = "user1new";

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUsername(searchId, newName));
    }

    @Test
    void UpdateUsernameAccountClosedTest() {
        User user = new User("user1", "");
        user.setStatus(UserStatus.USER_STATUS_CLOSED);
        Long searchId = 2L;

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class,
                () -> userService.updateUsername(searchId, "user1new"));
    }

    @Test
    void UpdatePasswordSucceedTest() {
        User user = new User("user1", "");
        Long searchId = 2L;
        String newPassword = "11111";
        String encodedPassword = "encoded-password";

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(eq(newPassword))).thenReturn(encodedPassword);
        User newUser = userService.updatePassword(searchId, newPassword);

        assertThat(newUser)
                .usingRecursiveComparison()
                .ignoringFields("password")
                .isEqualTo(user);
        assertThat(newUser.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    void UpdatePasswordNotFoundTest() {
        Long searchId = 2L;
        String newPassword = "11111";

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updatePassword(searchId, newPassword));
    }

    @Test
    void AddAdminRoleSucceedTest() {
        User user = new User("user1", "");
        Long searchId = 2L;

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.of(user));
        User updatedUser = userService.addAdminRole(searchId);

        assertThat(updatedUser.getRoles()).contains(Role.ROLE_ADMIN);
    }

    @Test
    void AddAdminRoleNotFoundTest() {
        Long searchId = 2L;

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.addAdminRole(searchId));
    }

    @Test
    void AddAdminRoleAccountClosedTest() {
        User user = new User("user1", "");
        user.setStatus(UserStatus.USER_STATUS_CLOSED);
        Long searchId = 2L;

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class,
                () -> userService.addAdminRole(searchId));
    }

    @Test
    void RemoveAdminRoleSucceedTest() {
        User user = new User("user1", "");
        user.addAdminRole();
        Long searchId = 2L;

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.of(user));
        when(userRepository.existsByRolesContainsAndStatusAndIdNot(
                eq(Role.ROLE_ADMIN), eq(UserStatus.USER_STATUS_ACTIVE), any()))
                .thenReturn(true);
        User updatedUser = userService.removeAdminRole(searchId);

        assertThat(updatedUser.getRoles()).doesNotContain(Role.ROLE_ADMIN);
        assertThat(updatedUser.getRoles()).contains(Role.ROLE_USER);
    }

    @Test
    void RemoveAdminRoleShouldRejectLastActiveAdminTest() {
        User user = new User("admin1", "");
        user.addAdminRole();
        Long searchId = 2L;

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.of(user));
        when(userRepository.existsByRolesContainsAndStatusAndIdNot(
                eq(Role.ROLE_ADMIN), eq(UserStatus.USER_STATUS_ACTIVE), any()))
                .thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> userService.removeAdminRole(searchId));
    }

    @Test
    void RemoveAdminRoleNotFoundTest() {
        Long searchId = 2L;

        when(userRepository.findById(eq(searchId))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.removeAdminRole(searchId));
    }

    @Test
    void SearchUsersTest() {
        List<User> users = List.of(
                new User("user1", ""),
                new User("user2", ""),
                new User("user3", "")
        );
        String searchUsername = "user";
        Role searchRole = Role.ROLE_USER;
        UserStatus searchStatus = UserStatus.USER_STATUS_ACTIVE;

        when(userRepository.findAll(any(Specification.class), eq(Pageable.unpaged(getUserSort()))))
                .thenReturn(new PageImpl<>(users));

        List<User> result = userService.searchUsers(searchUsername, searchRole, searchStatus, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(3);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(users);
    }

    @Test
    void SearchUsersShouldApplyDefaultSortToPagedQueryTest() {
        Pageable pageable = PageRequest.of(0, 20);
        Pageable expectedPageable = PageRequest.of(0, 20, getUserSort());

        when(userRepository.findAll(any(Specification.class), eq(expectedPageable)))
                .thenReturn(new PageImpl<>(List.of(), expectedPageable, 0));

        userService.searchUsers("user", Role.ROLE_USER, UserStatus.USER_STATUS_ACTIVE, pageable);

        verify(userRepository).findAll(any(Specification.class), eq(expectedPageable));
    }

    @Test
    void SearchUsersShouldApplyDefaultSortToUnpagedQueryTest() {
        when(userRepository.findAll(any(Specification.class), eq(Pageable.unpaged(getUserSort()))))
                .thenReturn(new PageImpl<>(List.of()));

        userService.searchUsers("user", Role.ROLE_USER, UserStatus.USER_STATUS_ACTIVE, Pageable.unpaged());

        verify(userRepository).findAll(any(Specification.class), eq(Pageable.unpaged(getUserSort())));
    }

    @Test
    void closeUserAccountSucceedTest() {
        User user = new User("user1", "");
        List<Reservation> reservations = List.of(
                new Reservation(user, new Room(),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0)),
                new Reservation(user, new Room(),
                        LocalDateTime.of(2026, 3, 1, 16, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 17, 0, 0, 0)));
        Long searchId = 2L;

        when(userRepository.findByIdWithLock(eq(searchId))).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserIdAndStartAfterAndScheduled(
                eq(searchId), any(LocalDateTime.class), eq(getOccupationSort())))
                .thenReturn(reservations);

        userService.closeUserAccount(searchId);
        assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
        assertThat(reservations.get(1).getStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_CLOSED);
    }

    @Test
    void closeUserAccountShouldRejectLastActiveAdminTest() {
        User user = new User("admin1", "");
        user.addAdminRole();
        Long searchId = 2L;

        when(userRepository.findByIdWithLock(eq(searchId))).thenReturn(Optional.of(user));
        when(userRepository.existsByRolesContainsAndStatusAndIdNot(
                eq(Role.ROLE_ADMIN), eq(UserStatus.USER_STATUS_ACTIVE), any()))
                .thenReturn(false);

        assertThrows(IllegalStateException.class, () -> userService.closeUserAccount(searchId));
    }

    @Test
    void closeUserAccountNotFoundTest() {
        Long searchId = 2L;
        when(userRepository.findByIdWithLock(eq(searchId))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.closeUserAccount(searchId));
    }

    private Sort getUserSort() {
        return Sort.by(
                Sort.Order.asc("id"),
                Sort.Order.asc("username"));
    }

    private Sort getOccupationSort() {
        return Sort.by(
                Sort.Order.asc("startTime"),
                Sort.Order.asc("endTime"),
                Sort.Order.asc("id"));
    }
}
