package com.zsj.RoomBooking.bootstrap;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private BootstrapAdminInitializer bootstrapAdminInitializer;

    @Test
    void runShouldCreateAdminWhenNoActiveAdminExistsTest() throws Exception {
        String adminUsername = "admin1";
        String adminPassword = "password1";
        ReflectionTestUtils.setField(bootstrapAdminInitializer, "username", adminUsername);
        ReflectionTestUtils.setField(bootstrapAdminInitializer, "password", adminPassword);

        when(userRepository.existsByRolesContainsAndStatus(Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE))
                .thenReturn(false);
        when(passwordEncoder.encode(adminPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bootstrapAdminInitializer.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User user = userCaptor.getValue();
        assertThat(user.getUsername()).isEqualTo(adminUsername);
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).contains(Role.ROLE_ADMIN, Role.ROLE_USER);
    }

    @Test
    void runShouldDoNothingWhenActiveAdminExistsTest() throws Exception {
        when(userRepository.existsByRolesContainsAndStatus(Role.ROLE_ADMIN, UserStatus.USER_STATUS_ACTIVE))
                .thenReturn(true);

        bootstrapAdminInitializer.run(new DefaultApplicationArguments(new String[0]));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void runShouldDoNothingWhenBootstrapCredentialsMissingTest() throws Exception {
        ReflectionTestUtils.setField(bootstrapAdminInitializer, "username", "");
        ReflectionTestUtils.setField(bootstrapAdminInitializer, "password", "");

        when(userRepository.existsByRolesContainsAndStatus(eq(Role.ROLE_ADMIN), eq(UserStatus.USER_STATUS_ACTIVE)))
                .thenReturn(false);

        bootstrapAdminInitializer.run(new DefaultApplicationArguments(new String[0]));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
