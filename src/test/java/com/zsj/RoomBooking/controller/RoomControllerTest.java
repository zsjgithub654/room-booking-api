package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.entity.Room;
import com.zsj.RoomBooking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
public class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    void getAllRooms() throws Exception {
        List<Room> roomsData = new ArrayList<>();
        roomsData.add(new Room("101", 12, "Building A"));
        roomsData.add(new Room("102", 4, "Building A"));
        roomsData.add(new Room("101", 6, "Building B"));
        when(roomService.getAllRooms()).thenReturn(roomsData);

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("101"))
                .andExpect(jsonPath("$[1].capacity").value(4))
                .andExpect(jsonPath("$[2].area").value("Building B"));
    }
}
