package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.AddClosureResponse;
import com.zsj.RoomBooking.model.ClosureRequest;
import com.zsj.RoomBooking.model.ClosureResponse;
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
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void getRoomTest() throws Exception {
        Long id = 1L;
        String name = "101";
        Integer capacity = 12;
        String area = "Building A";

        when(roomService.getRoom(id)).thenReturn(new RoomResponse(id, name, capacity, area));

        mockMvc.perform(get("/rooms/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.capacity").value(capacity))
                .andExpect(jsonPath("$.area").value(area));
    }

    @Test
    void addRoomTest() throws Exception {
        Long id = 1L;
        String name = "101";
        Integer capacity = 12;
        String area = "Building A";

        when(roomService.addRoom(any(RoomRequest.class))).thenReturn(new RoomResponse(id, name, capacity, area));

        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(new RoomRequest(name, capacity, area))))
                // HTTP 201 Created is the successful status of post request
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.capacity").value(capacity))
                .andExpect(jsonPath("$.area").value(area));
    }

    @Test
    void deleteRoomTest() throws Exception {
        Long id = 1L;
        String name = "101";
        Integer capacity = 12;
        String area = "Building A";
        when(roomService.deleteRoom(id)).thenReturn(new RoomResponse(id, name, capacity, area));

        mockMvc.perform(delete("/rooms/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.capacity").value(capacity))
                .andExpect(jsonPath("$.area").value(area));

        verify(roomService).deleteRoom(id);
    }
    @Test
    void updateRoomTest() throws Exception {
        Long id = 1L;
        String name = "101";
        Integer capacity = 30;
        String area = "Building A";

        when(roomService.updateRoom(any(Long.class), any(RoomRequest.class))).thenReturn(new RoomResponse(id, name, capacity, area));

        mockMvc.perform(put("/rooms/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(new RoomRequest(name, capacity, area))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.capacity").value(capacity))
                .andExpect(jsonPath("$.area").value(area));
    }

    @Test
    void getClosuresTest() throws Exception {
        Long userId = 1L;
        Long roomId = 2L;
        List<ClosureResponse> serviceResponse = new ArrayList<>();
        serviceResponse.add(new ClosureResponse(0L, userId, roomId, LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0), LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0)));
        serviceResponse.add(new ClosureResponse(1L, userId, roomId, LocalDateTime.of(2026, 3, 3, 8, 30, 0, 0), LocalDateTime.of(2026, 3, 3, 12, 0, 0, 0)));

        when(roomService.getClosures(eq(roomId))).thenReturn(serviceResponse);

        /* to compare LocalDateTime, parse response to dto object */
        String responseString = mockMvc.perform(get("/rooms/{roomId}/closures", roomId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ClosureResponse> controllerResponse =
                objectMapper.readValue(responseString, new TypeReference<List<ClosureResponse>>() {});

        for (int i = 0; i < controllerResponse.size(); i++) {
            assertThat(controllerResponse.get(i).id()).isEqualTo(serviceResponse.get(i).id());
            assertThat(controllerResponse.get(i).roomId()).isEqualTo(serviceResponse.get(i).roomId());
            assertThat(controllerResponse.get(i).userId()).isEqualTo(serviceResponse.get(i).userId());
            assertThat(controllerResponse.get(i).startTime()).isEqualTo(serviceResponse.get(i).startTime());
            assertThat(controllerResponse.get(i).endTime()).isEqualTo(serviceResponse.get(i).endTime());
        }
    }

    @Test
    void addClosureTest() throws Exception {
        Long userId = 1L;
        Long roomId = 2L;
        Long closureId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0);

        when(roomService.addClosure(eq(roomId), any(ClosureRequest.class)))
                .thenReturn(new AddClosureResponse(
                        new ClosureResponse(closureId, userId, roomId, startTime, endTime),
                        null));

        /* to compare LocalDateTime, parse response to dto object */
        String responseString = mockMvc.perform(post("/rooms/{roomId}/closures", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(new ClosureRequest(userId, startTime, endTime))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        AddClosureResponse response = objectMapper.readValue(responseString, AddClosureResponse.class);

        assertThat(response.closureResponse().roomId()).isEqualTo(roomId);
        assertThat(response.closureResponse().userId()).isEqualTo(userId);
        assertThat(response.closureResponse().startTime()).isEqualTo(startTime);
        assertThat(response.closureResponse().endTime()).isEqualTo(endTime);
        assertThat(response.canceledReservations()).isNull();
    }

    @Test
    void deleteClosureTest() throws Exception {
        /* data */
        Long roomId = 2L;
        Long closureId = 3L;
        Long userId = 4L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0);

        when(roomService.deleteClosure(roomId, closureId)).thenReturn(
                new ClosureResponse(closureId, userId, roomId, startTime, endTime));

        String responseString = mockMvc.perform(
                delete("/rooms/{roomId}/closures/{closureId}", roomId, closureId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        ClosureResponse response = objectMapper.readValue(responseString, ClosureResponse.class);

        assertThat(response.id()).isEqualTo(closureId);
        assertThat(response.roomId()).isEqualTo(roomId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.startTime()).isEqualTo(startTime);
        assertThat(response.endTime()).isEqualTo(endTime);
    }
}
