package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.RoomRequest;
import com.zsj.RoomBooking.model.RoomResponse;
import com.zsj.RoomBooking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
public class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAllRooms() throws Exception {
        List<RoomResponse> roomResponse = new ArrayList<>();
        roomResponse.add(new RoomResponse(1L, "101", 12, "Building A"));
        roomResponse.add(new RoomResponse(2L, "102", 4, "Building A"));
        roomResponse.add(new RoomResponse(3L, "101", 6, "Building B"));
        when(roomService.getAllRooms()).thenReturn(roomResponse);

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("101"))
                .andExpect(jsonPath("$[1].capacity").value(4))
                .andExpect(jsonPath("$[2].area").value("Building B"));
    }

    @Test
    void addRoom() throws Exception {
        RoomRequest roomRequest = new RoomRequest("101", 12, "Building A");
        when(roomService.addRoom(any(RoomRequest.class))).thenReturn(new RoomResponse(
                        1L, roomRequest.getName(), roomRequest.getCapacity(), roomRequest.getArea()));

        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(roomRequest)))
                // HTTP 201 Created is the successful status of post request
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(roomRequest.getName()))
                .andExpect(jsonPath("$.capacity").value(roomRequest.getCapacity()))
                .andExpect(jsonPath("$.area").value(roomRequest.getArea()));
    }
}
