package com.zsj.RoomBooking.security;

import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Required by Spring Security. Provide security a method to get the UserDetails object it needs. **/

@Service
public class RoomBookingUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .disabled(user.getStatus() != com.zsj.RoomBooking.model.UserStatus.USER_STATUS_ACTIVE)
                .build();
    }
}
