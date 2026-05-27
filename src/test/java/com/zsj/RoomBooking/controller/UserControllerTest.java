package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.UserMapper;
import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.dto.request.UpdatePasswordRequest;
import com.zsj.RoomBooking.model.dto.request.UpdateUsernameRequest;
import com.zsj.RoomBooking.model.dto.request.UserRequest;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserMapper.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getUserTest() throws Exception {
        Long id = 1L;
        String username = "user1";
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);

        when(userService.getUser(id)).thenReturn(new User(username, ""));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void addUserTest() throws Exception {
        String username = "user1";
        String password = "password1";

        when(userService.addUser(any(User.class))).thenReturn(new User(username, password));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserRequest(username, password))))
                // HTTP 201 Created is the successful status of post request
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void deleteUserTest() throws Exception {
        Long id = 1L;

        doNothing().when(userService).closeUserAccount(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(userService).closeUserAccount(id);
    }

    @Test
    void updateUsernameTest() throws Exception {
        Long id = 1L;
        String usernameNew = "user1NewName";

        when(userService.updateUsername(eq(id), eq(usernameNew)))
                .thenReturn(new User(usernameNew, ""));

        mockMvc.perform(patch("/users/{id}/username", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateUsernameRequest(usernameNew))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(usernameNew))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void updatePasswordTest() throws Exception {
        Long id = 1L;
        String password = "password1";

        when(userService.updatePassword(eq(id), eq(password)))
                .thenReturn(new User("user1", password));

        mockMvc.perform(patch("/users/{id}/password", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdatePasswordRequest(password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
        verify(userService).updatePassword(id, password);
    }
}
