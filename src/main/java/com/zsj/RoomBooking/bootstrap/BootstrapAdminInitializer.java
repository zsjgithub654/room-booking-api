package com.zsj.RoomBooking.bootstrap;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * To bootstrap the first admin user, who can then grant other users admin roles.
 */

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.username:}")
    private String username;

    @Value("${app.bootstrap.admin.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /* TODO: regex and length validation missing */
        if (username.isEmpty() || password.isEmpty()) {
            return;
        }
        if (userRepository.existsByRolesContainsAndStatus(Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE)) {
            return;
        }
        User user = new User(username, passwordEncoder.encode(password));
        user.addAdminRole();
        userRepository.save(user);
    }
}
