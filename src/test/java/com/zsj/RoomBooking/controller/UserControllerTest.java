package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.config.SecurityConfig;
import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.mapper.UserMapper;
import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.dto.request.ReservationRequest;
import com.zsj.RoomBooking.model.dto.request.UpdatePasswordRequest;
import com.zsj.RoomBooking.model.dto.request.UpdateUsernameRequest;
import com.zsj.RoomBooking.model.dto.request.UserRequest;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.security.CustomUserDetails;
import com.zsj.RoomBooking.service.ReservationService;
import com.zsj.RoomBooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
@Import({UserMapper.class, ReservationMapper.class, SecurityConfig.class})
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReservationService reservationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UsernamePasswordAuthenticationToken getAuthentication(Long userId, String username) {
        CustomUserDetails customUserDetails = new CustomUserDetails(userId, username, "password", Set.of(Role.ROLE_USER), true);
        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }

    private UsernamePasswordAuthenticationToken getAdminAuthentication(Long userId, String username) {
        CustomUserDetails customUserDetails = new CustomUserDetails(userId, username, "password", Set.of(Role.ROLE_ADMIN), true);
        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }

    @Test
    void getUserTest() throws Exception {
        Long id = 1L;
        String username = "user1";
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);

        when(userService.getUser(id)).thenReturn(new User(username, ""));

        mockMvc.perform(get("/users/{id}", id)
                        .with(authentication(getAdminAuthentication(1L, "admin1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void getCurrentUserTest() throws Exception {
        Long userId = 1L;
        String username = "user1";

        when(userService.getUser(eq(userId))).thenReturn(new User(username, ""));

        mockMvc.perform(get("/users/me")
                        .with(authentication(getAuthentication(userId, username))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void getCurrentUserShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUserReservationsTest() throws Exception {
        Long userId = 1L;
        String username = "user1";
        List<Reservation> reservations = List.of(
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0)),
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2026, 3, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 15, 30, 0, 0)));
        when(reservationService.searchReservations(eq(userId), eq(null), eq(null), eq(null))).thenReturn(reservations);

        String responseString = mockMvc.perform(get("/users/me/reservations")
                        .with(authentication(getAuthentication(userId, username))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ReservationResponse> responses = objectMapper.readValue(
                responseString,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ReservationResponse.class)
        );

        verify(reservationService).searchReservations(userId, null, null, null);
        assertThat(responses)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservations);
    }

    @Test
    void getCurrentUserReservationsShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/users/me/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserShouldRejectNonPositiveId() throws Exception {
        mockMvc.perform(get("/users/{id}", 0)
                        .with(authentication(getAdminAuthentication(1L, "admin1"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void getUserShouldRejectNonAdmin() throws Exception {
        mockMvc.perform(get("/users/{id}", 1L)
                        .with(authentication(getAuthentication(1L, "user1"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void addUserTest() throws Exception {
        String username = "user1";
        String password = "password1";

        when(userService.addUser(any(User.class))).thenReturn(new User(username, password));

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserRequest(username, password))))
                // HTTP 201 Created is the successful status of post request
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void addUserShouldRejectBlankUsername() throws Exception {
        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserRequest("   ", "password1"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void addUserShouldRejectInvalidUsernameCharacters() throws Exception {
        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserRequest("user 1", "password1!"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void addUserShouldRejectShortPassword() throws Exception {
        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserRequest("user1", "short1!"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void addCurrentUserReservationTest() throws Exception {
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);
        Reservation reservation = new Reservation(new User(), new Room(), startTime, endTime);
        when(reservationService.addReservation(eq(userId), eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(reservation);

        String responseString = mockMvc.perform(post("/users/me/reservations")
                        .with(csrf())
                        .with(authentication(getAuthentication(userId, "user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse response = objectMapper.readValue(responseString, ReservationResponse.class);

        verify(reservationService).addReservation(userId, roomId, startTime, endTime);
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservation);
    }

    @Test
    void addCurrentUserReservationShouldRequireAuthentication() throws Exception {
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(post("/users/me/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservationForUserShouldRejectNonAdmin() throws Exception {
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(post("/users/{id}/reservations", userId)
                        .with(csrf())
                        .with(authentication(getAuthentication(1L, "user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservationForUserTest() throws Exception {
        Long adminId = 1L;
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);
        Reservation reservation = new Reservation(new User(), new Room(), startTime, endTime);
        when(reservationService.addReservation(eq(userId), eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(reservation);

        String responseString = mockMvc.perform(post("/users/{id}/reservations", userId)
                        .with(csrf())
                        .with(authentication(getAdminAuthentication(adminId, "admin1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse response = objectMapper.readValue(responseString, ReservationResponse.class);

        verify(reservationService).addReservation(userId, roomId, startTime, endTime);
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservation);
    }

    @Test
    void deleteUserTest() throws Exception {
        Long id = 1L;

        doNothing().when(userService).closeUserAccount(id);

        mockMvc.perform(delete("/users/{id}", id)
                        .with(csrf())
                        .with(authentication(getAdminAuthentication(1L, "admin1"))))
                .andExpect(status().isNoContent());

        verify(userService).closeUserAccount(id);
    }

    @Test
    void deleteCurrentUserTest() throws Exception {
        Long userId = 1L;
        String username = "user1";

        doNothing().when(userService).closeUserAccount(eq(userId));

        mockMvc.perform(delete("/users/me")
                        .with(csrf())
                        .with(authentication(getAuthentication(userId, username))))
                .andExpect(status().isNoContent());

        verify(userService).closeUserAccount(userId);
    }

    @Test
    void updateUsernameTest() throws Exception {
        Long id = 1L;
        String usernameNew = "user1NewName";

        when(userService.updateUsername(eq(id), eq(usernameNew)))
                .thenReturn(new User(usernameNew, ""));

        mockMvc.perform(patch("/users/{id}/username", id)
                        .with(csrf())
                        .with(authentication(getAdminAuthentication(1L, "admin1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateUsernameRequest(usernameNew))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(usernameNew))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void updateCurrentUsernameTest() throws Exception {
        Long userId = 1L;
        String username = "user1";
        String usernameNew = "user1NewName";

        when(userService.updateUsername(eq(userId), eq(usernameNew)))
                .thenReturn(new User(usernameNew, ""));

        mockMvc.perform(patch("/users/me/username")
                        .with(csrf())
                        .with(authentication(getAuthentication(userId, username)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateUsernameRequest(usernameNew))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(usernameNew))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void updateUsernameShouldRejectBlankUsername() throws Exception {
        Long id = 1L;

        mockMvc.perform(patch("/users/{id}/username", id)
                        .with(csrf())
                        .with(authentication(getAdminAuthentication(1L, "admin1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateUsernameRequest("   "))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updateUsernameShouldRejectUsernameLongerThanTwentyChars() throws Exception {
        Long id = 1L;

        mockMvc.perform(patch("/users/{id}/username", id)
                        .with(csrf())
                        .with(authentication(getAdminAuthentication(1L, "admin1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateUsernameRequest("this_username_is_too_long"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updatePasswordTest() throws Exception {
        Long id = 1L;
        String password = "password1";

        when(userService.updatePassword(eq(id), eq(password)))
                .thenReturn(new User("user1", password));

        mockMvc.perform(patch("/users/{id}/password", id)
                        .with(csrf())
                        .with(authentication(getAdminAuthentication(1L, "admin1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdatePasswordRequest(password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
        verify(userService).updatePassword(id, password);
    }

    @Test
    void updateCurrentPasswordTest() throws Exception {
        Long userId = 1L;
        String username = "user1";
        String password = "password1";

        when(userService.updatePassword(eq(userId), eq(password)))
                .thenReturn(new User(username, password));

        mockMvc.perform(patch("/users/me/password")
                        .with(csrf())
                        .with(authentication(getAuthentication(userId, username)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdatePasswordRequest(password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
        verify(userService).updatePassword(userId, password);
    }

    @Test
    void updatePasswordShouldRejectBlankPassword() throws Exception {
        Long id = 1L;

        mockMvc.perform(patch("/users/{id}/password", id)
                        .with(csrf())
                        .with(authentication(getAdminAuthentication(1L, "admin1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdatePasswordRequest("   "))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}
