package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsernameAndRoleAndStatusHasResultTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRoleAndStatus(
                "user", Role.ROLE_USER, UserStatus.USER_STATUS_ACTIVE, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void findByUsernameAndRoleAndStatusWithoutUsernameHasResultTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRoleAndStatus(
                null, Role.ROLE_USER, UserStatus.USER_STATUS_ACTIVE, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void findByUsernameAndRoleAndStatusWithoutRoleHasResultTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRoleAndStatus(
                "user", null, UserStatus.USER_STATUS_ACTIVE, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void findByUsernameAndRoleAndStatusWithoutStatusHasResultTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRoleAndStatus(
                "user", Role.ROLE_USER, null, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void findByUsernameAndRoleAndStatusUsernameNotFoundTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRoleAndStatus(
                "user2", Role.ROLE_USER, UserStatus.USER_STATUS_ACTIVE, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(0);
    }

    @Test
    void findByUsernameAndRoleAndStatusRoleNotFoundTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRoleAndStatus(
                "user", Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(0);
    }

    @Test
    void findByUsernameAndRoleAndStatusStatusNotFoundTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRoleAndStatus(
                "user", Role.ROLE_USER, UserStatus.USER_STATUS_CLOSED, Pageable.unpaged()).getContent();
        assertThat(result).hasSize(0);
    }

    @Test
    void findByUsernameAndRoleAndStatusPagedTest() {
        userRepository.save(new User("user1", ""));
        userRepository.save(new User("user2", ""));
        userRepository.save(new User("user3", ""));

        Page<User> result = userRepository.findByUsernameAndRoleAndStatus(
                "user",
                Role.ROLE_USER,
                UserStatus.USER_STATUS_ACTIVE,
                PageRequest.of(1, 2, Sort.by("username")));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("user3");
    }
}
