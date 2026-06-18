package com.zsj.RoomBooking.bootstrap;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BootstrapAdminInitializerTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Validator validator;

    @Test
    void runShouldCreateAdminWhenNoActiveAdminExistsTest() {
        BootstrapAdminProperties bootstrapAdminProperties = new BootstrapAdminProperties("admin1", "password1");
        BootstrapAdminInitializer bootstrapAdminInitializer = new BootstrapAdminInitializer(
                userRepository,
                passwordEncoder,
                validator,
                bootstrapAdminProperties);

        when(userRepository.existsByRolesContainsAndStatus(Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE))
                .thenReturn(false);
        when(validator.validate(bootstrapAdminProperties)).thenReturn(Set.of());
        when(passwordEncoder.encode(bootstrapAdminProperties.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bootstrapAdminInitializer.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User user = userCaptor.getValue();
        assertThat(user.getUsername()).isEqualTo(bootstrapAdminProperties.username());
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).contains(Role.ROLE_ADMIN, Role.ROLE_USER);
    }

    @Test
    void runShouldDoNothingWhenActiveAdminExistsTest() {
        BootstrapAdminProperties bootstrapAdminProperties = new BootstrapAdminProperties("admin1", "password1");
        BootstrapAdminInitializer bootstrapAdminInitializer = new BootstrapAdminInitializer(
                userRepository,
                passwordEncoder,
                validator,
                bootstrapAdminProperties);

        when(userRepository.existsByRolesContainsAndStatus(Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE))
                .thenReturn(true);

        bootstrapAdminInitializer.run(new DefaultApplicationArguments(new String[0]));

        verify(validator, never()).validate(any(BootstrapAdminProperties.class));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void runShouldIgnoreCredentialsWhenActiveAdminExistsTest() {
        BootstrapAdminInitializer bootstrapAdminInitializer = new BootstrapAdminInitializer(
                userRepository,
                passwordEncoder,
                validator,
                new BootstrapAdminProperties("", ""));

        when(userRepository.existsByRolesContainsAndStatus(Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE))
                .thenReturn(true);

        bootstrapAdminInitializer.run(new DefaultApplicationArguments(new String[0]));

        verify(validator, never()).validate(any(BootstrapAdminProperties.class));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void runShouldThrowWhenBootstrapCredentialsMissingTest() {
        BootstrapAdminInitializer bootstrapAdminInitializer = new BootstrapAdminInitializer(
                userRepository,
                passwordEncoder,
                validator,
                new BootstrapAdminProperties("", ""));

        when(userRepository.existsByRolesContainsAndStatus(Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE))
                .thenReturn(false);

        assertThatThrownBy(() -> bootstrapAdminInitializer.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Bootstrap admin username is required.");

        verify(userRepository).existsByRolesContainsAndStatus(eq(Role.ROLE_ADMIN), eq(UserStatus.USER_STATUS_ACTIVE));
        verify(validator, never()).validate(any(BootstrapAdminProperties.class));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void runShouldThrowWhenBootstrapCredentialsInvalidTest() {
        BootstrapAdminProperties bootstrapAdminProperties = new BootstrapAdminProperties("ad", "password1");
        BootstrapAdminInitializer bootstrapAdminInitializer = new BootstrapAdminInitializer(
                userRepository,
                passwordEncoder,
                validator,
                bootstrapAdminProperties);
        @SuppressWarnings("unchecked")
        ConstraintViolation<BootstrapAdminProperties> violation = (ConstraintViolation<BootstrapAdminProperties>) org.mockito.Mockito.mock(ConstraintViolation.class);

        when(userRepository.existsByRolesContainsAndStatus(Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE))
                .thenReturn(false);
        when(validator.validate(bootstrapAdminProperties)).thenReturn(Set.of(violation));
        when(violation.getMessage()).thenReturn("size must be between 3 and 20");

        assertThatThrownBy(() -> bootstrapAdminInitializer.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("size must be between 3 and 20");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
