package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.RoomRequest;
import com.zsj.RoomBooking.model.RoomResponse;
import com.zsj.RoomBooking.model.SearchRoomRequest;
import com.zsj.RoomBooking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    void searchRoomsTest() throws Exception {
        /* response */
        List<RoomResponse> roomResponse = new ArrayList<>();
        roomResponse.add(new RoomResponse(1L, "101", 12, "Building A"));
        roomResponse.add(new RoomResponse(2L, "102", 4, "Building A"));
        roomResponse.add(new RoomResponse(3L, "101", 6, "Building B"));
        /* request */
        SearchRoomRequest roomRequest = new SearchRoomRequest(null,
                2, 20,
                null, LocalDate.now(),
                null, null);
        /* mock behavior */
        when(roomService.searchRooms(any(SearchRoomRequest.class))).thenReturn(roomResponse);
        /* verify return value */
        mockMvc.perform(get("/rooms")
                .param("minCapacity", roomRequest.minCapacity().toString())
                .param("maxCapacity", roomRequest.maxCapacity().toString())
                .param("date", roomRequest.date().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("101"))
                .andExpect(jsonPath("$[1].capacity").value(4))
                .andExpect(jsonPath("$[2].area").value("Building B"));
        /* verify params passed to service */
        verify(roomService).searchRooms(roomRequest);
    }

    @Test
    void addRoomTest() throws Exception {
        RoomRequest roomRequest = new RoomRequest("101", 12, "Building A");
        when(roomService.addRoom(any(RoomRequest.class))).thenReturn(new RoomResponse(
                        1L, roomRequest.name(), roomRequest.capacity(), roomRequest.area()));

        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(roomRequest)))
                // HTTP 201 Created is the successful status of post request
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(roomRequest.name()))
                .andExpect(jsonPath("$.capacity").value(roomRequest.capacity()))
                .andExpect(jsonPath("$.area").value(roomRequest.area()));
    }

    @Test
    void deleteRoomTest() throws Exception {
        Long id = 1L;
        when(roomService.deleteRoom(any(Long.class))).thenReturn(new RoomResponse(id, "101", 12, "Building A"));

        mockMvc.perform(delete("/rooms/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        verify(roomService).deleteRoom(id);
    }
}
