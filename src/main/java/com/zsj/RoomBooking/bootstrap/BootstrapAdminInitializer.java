package com.zsj.RoomBooking.bootstrap;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * To bootstrap the first admin user, who can then grant other users admin roles.
 */

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {
    private static final String BOOTSTRAP_ADMIN_USERNAME_REQUIRED = "Bootstrap admin username is required.";
    private static final String BOOTSTRAP_ADMIN_PASSWORD_REQUIRED = "Bootstrap admin password is required.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    public BootstrapAdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Validator validator,
            BootstrapAdminProperties bootstrapAdminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.bootstrapAdminProperties = bootstrapAdminProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRolesContainsAndStatus(Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE)) {
            return;
        }
        /* admin credentials are required if no active admin exists */
        if (bootstrapAdminProperties.username() == null || bootstrapAdminProperties.username().isBlank()) {
            throw new IllegalStateException(BOOTSTRAP_ADMIN_USERNAME_REQUIRED);
        }
        if (bootstrapAdminProperties.password() == null || bootstrapAdminProperties.password().isBlank()) {
            throw new IllegalStateException(BOOTSTRAP_ADMIN_PASSWORD_REQUIRED);
        }
        /* validate properties */
        Set<ConstraintViolation<BootstrapAdminProperties>> violations = validator.validate(bootstrapAdminProperties);
        if (!violations.isEmpty()) {
            throw new IllegalStateException(violations.iterator().next().getMessage());
        }
        User user = new User(
                bootstrapAdminProperties.username(),
                passwordEncoder.encode(bootstrapAdminProperties.password()));
        user.addAdminRole();
        userRepository.save(user);
    }
}
