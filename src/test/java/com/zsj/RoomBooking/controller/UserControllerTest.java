package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.config.SecurityConfig;
import com.zsj.RoomBooking.mapper.UserMapper;
import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.dto.request.UpdatePasswordRequest;
import com.zsj.RoomBooking.model.dto.request.UpdateUsernameRequest;
import com.zsj.RoomBooking.model.dto.request.UserRequest;
import com.zsj.RoomBooking.model.dto.response.UserResponse;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.security.CustomUserDetails;
import com.zsj.RoomBooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
@Import({UserMapper.class, SecurityConfig.class})
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getUserTest() throws Exception {
        Long userId = 1L;
        String username = "user1";
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);

        when(userService.getUser(userId)).thenReturn(new User(username, ""));

        mockMvc.perform(get("/users/{userId}", userId)
                        .with(user("admin1").roles("ADMIN")))
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
    void searchUserTest() throws Exception {
        User user = new User("user1", "");

        when(userService.searchUsers(eq("user"), eq(null), eq(null), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(java.util.List.of(user), PageRequest.of(0, 20), 1));

        String responseString = mockMvc.perform(get("/users")
                        .with(user("admin1").roles("ADMIN"))
                        .param("username", "user")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value("user1"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).searchUsers("user", null, null, PageRequest.of(0, 20));
        TypeReference<List<UserResponse>> typeReference = new TypeReference<List<UserResponse>>() {
        };
        String contentString = objectMapper.readTree(responseString).get("content").toString();
        List<UserResponse> responses = objectMapper.readValue(contentString, typeReference);
        assertThat(responses)
                .usingRecursiveComparison()
                .isEqualTo(List.of(user));
    }

    @Test
    void getUserShouldRejectNonPositiveId() throws Exception {
        mockMvc.perform(get("/users/{id}", 0)
                        .with(user("admin1").roles("ADMIN")))
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
    void deleteUserTest() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).closeUserAccount(userId);

        mockMvc.perform(delete("/users/{userId}", userId)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(userService).closeUserAccount(userId);
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
        Long userId = 1L;
        String usernameNew = "user1NewName";

        when(userService.updateUsername(eq(userId), eq(usernameNew)))
                .thenReturn(new User(usernameNew, ""));

        mockMvc.perform(patch("/users/{userId}/username", userId)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
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
        Long userId = 1L;

        mockMvc.perform(patch("/users/{userId}/username", userId)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateUsernameRequest("   "))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updateUsernameShouldRejectUsernameLongerThanTwentyChars() throws Exception {
        Long userId = 1L;

        mockMvc.perform(patch("/users/{userId}/username", userId)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateUsernameRequest("this_username_is_too_long"))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updatePasswordTest() throws Exception {
        Long userId = 1L;
        String password = "password1";

        when(userService.updatePassword(eq(userId), eq(password)))
                .thenReturn(new User("user1", password));

        mockMvc.perform(patch("/users/{userId}/password", userId)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdatePasswordRequest(password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
        verify(userService).updatePassword(userId, password);
    }

    @Test
    void addAdminRoleTest() throws Exception {
        Long userId = 1L;
        User user = new User("user1", "");
        user.addAdminRole();

        when(userService.addAdminRole(eq(userId))).thenReturn(user);

        mockMvc.perform(patch("/users/{userId}/roles/admin", userId)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_ADMIN.name())));

        verify(userService).addAdminRole(userId);
    }

    @Test
    void addAdminRoleShouldRejectNonAdmin() throws Exception {
        mockMvc.perform(patch("/users/{id}/roles/admin", 1L)
                        .with(csrf())
                        .with(authentication(getAuthentication(1L, "user1"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void removeAdminRoleTest() throws Exception {
        Long userId = 1L;
        User user = new User("user1", "");
        String operatorUsername = "admin1";

        when(userService.removeAdminRole(eq(userId), eq(operatorUsername))).thenReturn(user);

        mockMvc.perform(delete("/users/{userId}/roles/admin", userId)
                        .with(csrf())
                        .with(user(operatorUsername).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));

        verify(userService).removeAdminRole(userId, operatorUsername);
    }

    @Test
    void removeAdminRoleShouldRejectNonAdmin() throws Exception {
        mockMvc.perform(delete("/users/{id}/roles/admin", 1L)
                        .with(csrf())
                        .with(authentication(getAuthentication(1L, "user1"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
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
        Long userId = 1L;

        mockMvc.perform(patch("/users/{userId}/password", userId)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdatePasswordRequest("   "))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(Long userId, String username) {
        CustomUserDetails customUserDetails = new CustomUserDetails(userId, username, "password", Set.of(Role.ROLE_USER), true);
        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }
}
