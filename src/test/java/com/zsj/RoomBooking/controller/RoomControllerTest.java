package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.dto.request.SearchRoomRequest;
import com.zsj.RoomBooking.model.dto.response.AddClosureResponse;
import com.zsj.RoomBooking.model.dto.response.ClosureResponse;
import com.zsj.RoomBooking.model.dto.request.RoomRequest;
import com.zsj.RoomBooking.model.dto.response.DeleteRoomResponse;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
import com.zsj.RoomBooking.model.dto.response.RoomResponse;
import com.zsj.RoomBooking.model.dto.request.SearchAvailabilityRequest;
import com.zsj.RoomBooking.model.dto.request.TimeRangeRequest;
import com.zsj.RoomBooking.model.dto.response.SearchAvailabilityResponse;
import com.zsj.RoomBooking.model.dto.response.TimeRangeResponse;
import com.zsj.RoomBooking.service.ClosureService;
import com.zsj.RoomBooking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
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

    @MockitoBean
    private ClosureService closureService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void searchRoomsTest() throws Exception {
        /* request */
        SearchRoomRequest request = new SearchRoomRequest(null,
                2, 20,
                null);
        /* mock service response */
        List<RoomResponse> serviceResponse = List.of(
        new RoomResponse(1L, "101", 12, "Building A"),
        new RoomResponse(2L, "102", 4, "Building A"),
        new RoomResponse(3L, "101", 6, "Building B"));
        when(roomService.searchRooms(any(SearchRoomRequest.class))).thenReturn(serviceResponse);
        /* perform */
        String responseString = mockMvc.perform(get("/rooms")
                        .param("minCapacity", request.minCapacity().toString())
                        .param("maxCapacity", request.maxCapacity().toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        /* verify params passed to service */
        verify(roomService).searchRooms(request);
        /* verify response */
        List<RoomResponse> controllerResponse =
                objectMapper.readValue(responseString, new TypeReference<List<RoomResponse>>() {
                });
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }

    @Test
    void searchAvailabilitiesTest() throws Exception {
        /* request */
        SearchAvailabilityRequest request = new SearchAvailabilityRequest(null,
                2, 20,
                null,
                LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0),
                LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0));
        /* mock service response */
        List<SearchAvailabilityResponse> serviceResponse = List.of(
                new SearchAvailabilityResponse(new RoomResponse(1L, "101", 12, "Building A"),
                        List.of(
                                new TimeRangeResponse(LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0),
                                        LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0)),
                                new TimeRangeResponse(LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0),
                                        LocalDateTime.of(2026, 3, 1, 17, 0, 0, 0))
                        )
                ),
                new SearchAvailabilityResponse(new RoomResponse(1L, "102", 4, "Building B"),
                        List.of(
                                new TimeRangeResponse(LocalDateTime.of(2026, 3, 2, 8, 0, 0, 0),
                                        LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0))
                        )
                ));
        when(roomService.searchAvailabilities(any(SearchAvailabilityRequest.class))).thenReturn(serviceResponse);
        /* perform */
        String responseString = mockMvc.perform(get("/rooms/availabilities")
                        .param("minCapacity", request.minCapacity().toString())
                        .param("maxCapacity", request.maxCapacity().toString())
                        .param("startTime", request.startTime().toString())
                        .param("endTime", request.endTime().toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<SearchAvailabilityResponse> controllerResponse =
                objectMapper.readValue(responseString, new TypeReference<List<SearchAvailabilityResponse>>() {});
        /* verify params passed to service */
        verify(roomService).searchAvailabilities(request);
        /* verify response */
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }

    @Test
    void getRoomTest() throws Exception {
        /* request */
        Long id = 1L;
        /* mock response */
        String name = "101";
        Integer capacity = 12;
        String area = "Building A";
        when(roomService.getRoom(id)).thenReturn(new RoomResponse(id, "101", 12, "Building A"));
        /* perform and verify */
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
        /* request */
        Long roomId = 1L;
        /* mock service response */
        DeleteRoomResponse serviceResponse = new DeleteRoomResponse(
                roomId,
                List.of(
                        new ReservationResponse(0L, 10L, 100L,
                                LocalDateTime.of(2300, 1, 1, 10, 0, 0, 0),
                                LocalDateTime.of(2300, 1, 1, 12, 0, 0, 0),
                                ReservationStatus.RESERVATION_STATUS_CLOSED),
                        new ReservationResponse(1L, 11L, 101L,
                                LocalDateTime.of(2300, 1, 2, 8, 0, 0, 0),
                                LocalDateTime.of(2300, 1, 2, 9, 0, 0, 0),
                                ReservationStatus.RESERVATION_STATUS_CLOSED)
                ));
        when(roomService.deleteRoom(eq(roomId))).thenReturn(serviceResponse);
        /* perform */
        String responseString = mockMvc.perform(delete("/rooms/{id}", roomId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        /* verify request */
        verify(roomService).deleteRoom(roomId);
        /* verify response */
        DeleteRoomResponse controllerResponse =objectMapper.readValue(responseString, DeleteRoomResponse.class);
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }

    @Test
    void updateRoomTest() throws Exception {
        Long id = 1L;
        String name = "101";
        Integer capacity = 30;
        String area = "Building A";

        when(roomService.updateRoom(any(Long.class), any(RoomRequest.class)))
                .thenReturn(new RoomResponse(id, name, capacity, area));

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
        /* request */
        Long roomId = 2L;
        /* mock service response */
        List<ClosureResponse> serviceResponse = List.of(
                new ClosureResponse(0L, 1L, roomId, LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0), LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0)),
                new ClosureResponse(1L, 1L, roomId, LocalDateTime.of(2026, 3, 3, 8, 30, 0, 0), LocalDateTime.of(2026, 3, 3, 12, 0, 0, 0)));
        when(closureService.getClosuresForRoom(eq(roomId))).thenReturn(serviceResponse);
        /* perform, to compare LocalDateTime, parse response to dto object */
        String responseString = mockMvc.perform(get("/rooms/{roomId}/closures", roomId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ClosureResponse> controllerResponse =
                objectMapper.readValue(responseString, new TypeReference<List<ClosureResponse>>() {});
        /* verify */
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }

    @Test
    void addClosureTest() throws Exception {
        /* request */
        Long userId = 1L;
        Long roomId = 2L;
        LocalDateTime startTime = LocalDateTime.of(2300, 1, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 1, 10, 10, 30, 0, 0);
        /* mock service response */
        AddClosureResponse serviceResponse = new AddClosureResponse(
                new ClosureResponse(3L, roomId, userId, startTime, endTime),
                List.of(
                        new ReservationResponse(0L, 10L, 100L,
                                LocalDateTime.of(2300, 1, 1, 10, 0, 0, 0),
                                LocalDateTime.of(2300, 1, 1, 12, 0, 0, 0),
                                ReservationStatus.RESERVATION_STATUS_CLOSED),
                        new ReservationResponse(1L, 11L, 101L,
                                LocalDateTime.of(2300, 1, 2, 8, 0, 0, 0),
                                LocalDateTime.of(2300, 1, 2, 9, 0, 0, 0),
                                ReservationStatus.RESERVATION_STATUS_CLOSED)
                ));
        /* mock */
        when(closureService.addClosure(eq(roomId), eq(userId), any(TimeRangeRequest.class)))
                .thenReturn(serviceResponse);

        /* perform, in order to compare LocalDateTime, need to parse response to dto object */
        String responseString = mockMvc.perform(post("/rooms/{roomId}/closures", roomId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new TimeRangeRequest(startTime, endTime))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        AddClosureResponse controllerResponse = objectMapper.readValue(responseString, AddClosureResponse.class);
        /* verify */
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }
}
