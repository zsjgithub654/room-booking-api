package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void filterByUsernameContainsTest() {
        User user = new User("user1", "");
        userRepository.save(user);

        List<User> result = userRepository.findAll(UserSpecifications.usernameContains("USER"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void filterByRoleTest() {
        User admin = new User("admin1", "");
        admin.addAdminRole();
        userRepository.save(admin);
        userRepository.save(new User("user1", ""));

        List<User> result = userRepository.findAll(UserSpecifications.hasRole(Role.ROLE_ADMIN));

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(admin);
    }

    @Test
    void filterByStatusTest() {
        User activeUser = new User("user1", "");
        User closedUser = new User("user2", "");
        closedUser.setStatus(UserStatus.USER_STATUS_CLOSED);
        userRepository.save(activeUser);
        userRepository.save(closedUser);

        List<User> result = userRepository.findAll(UserSpecifications.hasStatus(UserStatus.USER_STATUS_CLOSED));

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(closedUser);
    }

    @Test
    void findAllWithCombinedUserSpecificationsPagedTest() {
        User firstUser = new User("user1", "");
        User secondUser = new User("user2", "");
        User thirdUser = new User("user3", "");
        userRepository.save(firstUser);
        userRepository.save(secondUser);
        userRepository.save(thirdUser);

        Page<User> result = userRepository.findAll(
                UserSpecifications.usernameContains("User")
                        .and(UserSpecifications.hasRole(Role.ROLE_USER))
                        .and(UserSpecifications.hasStatus(UserStatus.USER_STATUS_ACTIVE)),
                PageRequest.of(1, 2, Sort.by("username")));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("user3");
    }
}
