package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsernameAndRoleHasResultTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRole("user", Role.ROLE_USER);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void findByUsernameAndRoleWithoutUsernameHasResultTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRole(null, Role.ROLE_USER);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void findByUsernameAndRoleWithoutRoleHasResultTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRole("user", null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void findByUsernameAndRoleUsernameNotFoundTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRole("user2", Role.ROLE_USER);
        assertThat(result).hasSize(0);
    }

    @Test
    void findByUsernameAndRoleRoleNotFoundTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findByUsernameAndRole("user", Role.ROLE_ADMIN);
        assertThat(result).hasSize(0);
    }
}
