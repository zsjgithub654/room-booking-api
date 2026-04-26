package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.Role;
import com.zsj.RoomBooking.model.dto.request.UserRequest;
import com.zsj.RoomBooking.model.dto.response.UserResponse;
import com.zsj.RoomBooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
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

        when(userService.getUser(id)).thenReturn(new UserResponse(id, username, roles));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasSize(roles.size())))
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void addUserTest() throws Exception {
        Long id = 1L;
        String username = "user1";
        String password = "password1";
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);

        when(userService.addUser(any(UserRequest.class)))
                .thenReturn(new UserResponse(id, username, roles));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserRequest(username, password))))
                // HTTP 201 Created is the successful status of post request
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasSize(roles.size())))
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));
    }

    @Test
    void deleteUserTest() throws Exception {
        Long id = 1L;
        String username = "user1";
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);

        when(userService.deleteUser(id)).thenReturn(new UserResponse(id, username, roles));

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasSize(roles.size())))
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())));

        verify(userService).deleteUser(id);
    }

    @Test
    void updateRoomTest() throws Exception {
        Long id = 1L;
        String username = "user1NewName";
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        roles.add(Role.ROLE_ADMIN);

        when(userService.updateUser(any(Long.class), any(UserRequest.class)))
                .thenReturn(new UserResponse(id, username, roles));

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UserResponse(id, username, roles))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles", hasSize(roles.size())))
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_USER.name())))
                .andExpect(jsonPath("$.roles", hasItem(Role.ROLE_ADMIN.name())));
    }
}
