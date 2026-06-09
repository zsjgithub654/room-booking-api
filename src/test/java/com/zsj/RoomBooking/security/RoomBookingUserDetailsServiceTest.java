package com.zsj.RoomBooking.security;

import com.zsj.RoomBooking.model.UserStatus;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoomBookingUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoomBookingUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameSucceedTest() {
        User user = new User("user1", "encoded-password");
        when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        assertThat(userDetails.getUsername()).isEqualTo(user.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsernameClosedUserTest() {
        User user = new User("user1", "encoded-password");
        user.setStatus(UserStatus.USER_STATUS_CLOSED);
        when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsernameNotFoundTest() {
        when(userRepository.findByUsername(eq("missing"))).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing"));
        assertThat(exception.getMessage()).isEqualTo("User not found.");
    }
}
