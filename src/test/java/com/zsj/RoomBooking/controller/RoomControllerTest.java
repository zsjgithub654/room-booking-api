package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.AvailabilityMapper;
import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.mapper.RoomMapper;
import com.zsj.RoomBooking.mapper.TimeRangeMapper;
import com.zsj.RoomBooking.model.Availability;
import com.zsj.RoomBooking.model.TimeRange;
import com.zsj.RoomBooking.model.dto.request.SearchRoomRequest;
import com.zsj.RoomBooking.model.dto.request.RoomRequest;
import com.zsj.RoomBooking.model.dto.response.DeleteRoomResponse;
import com.zsj.RoomBooking.model.dto.response.RoomResponse;
import com.zsj.RoomBooking.model.dto.request.SearchAvailabilityRequest;
import com.zsj.RoomBooking.model.dto.response.AvailabilityResponse;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.service.ClosureService;
import com.zsj.RoomBooking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
@Import({RoomMapper.class,
        ReservationMapper.class,
        AvailabilityMapper.class,
        TimeRangeMapper.class
})
public class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void searchRoomsTest() throws Exception {
        /* request */
        SearchRoomRequest request = new SearchRoomRequest(null,
                2, 20,
                null);
        /* mock service result */
        List<Room> rooms = List.of(
                new Room("101", 12, "Building A"),
                new Room("102", 4, "Building A"),
                new Room("101", 6, "Building B")
        );
        when(roomService.searchRooms(
                eq(request.name()),
                eq(request.minCapacity()),
                eq(request.maxCapacity()),
                eq(request.area())
        )).thenReturn(rooms);
        /* perform */
        String responseString = mockMvc.perform(get("/rooms")
                        .param("minCapacity", request.minCapacity().toString())
                        .param("maxCapacity", request.maxCapacity().toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        /* verify params passed to service */
        verify(roomService).searchRooms(request.name(), request.minCapacity(), request.maxCapacity(), request.area());
        /* verify response */
        List<RoomResponse> responses =
                objectMapper.readValue(responseString, new TypeReference<List<RoomResponse>>() {
                });
        assertThat(responses)
                .usingRecursiveComparison()
                .isEqualTo(rooms);
    }

    @Test
    void searchAvailabilitiesTest() throws Exception {
        /* request */
        SearchAvailabilityRequest request = new SearchAvailabilityRequest(null,
                2, 20,
                null,
                LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0),
                LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0));
        /* mock service result */
        List<Availability> availabilities = List.of(
                new Availability(new Room("101", 12, "Building A"),
                        List.of(
                                new TimeRange(LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0),
                                        LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0)),
                                new TimeRange(LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0),
                                        LocalDateTime.of(2026, 3, 1, 17, 0, 0, 0))
                        )
                ),
                new Availability(new Room("102", 4, "Building B"),
                        List.of(
                                new TimeRange(LocalDateTime.of(2026, 3, 2, 8, 0, 0, 0),
                                        LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0))
                        )
                ));
        when(roomService.searchAvailabilities(
                request.name(),
                request.minCapacity(), request.maxCapacity(),
                request.area(),
                request.startTime(), request.endTime())
        ).thenReturn(availabilities);
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
        List<AvailabilityResponse> responses =
                objectMapper.readValue(responseString, new TypeReference<List<AvailabilityResponse>>() {
                });
        /* verify params passed to service */
        verify(roomService).searchAvailabilities(
                request.name(), request.minCapacity(), request.maxCapacity(), request.area(),
                request.startTime(), request.endTime());
        /* verify response */
        assertThat(responses)
                .usingRecursiveComparison()
                .isEqualTo(availabilities);
    }

    @Test
    void getRoomTest() throws Exception {
        /* mock service result */
        Long id = 0L;
        Room room = new Room("101", 12, "Building A");
        when(roomService.getRoom(id)).thenReturn(room);
        /* perform and verify */
        mockMvc.perform(get("/rooms/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(room.getName()))
                .andExpect(jsonPath("$.capacity").value(room.getCapacity()))
                .andExpect(jsonPath("$.area").value(room.getArea()));
    }

    @Test
    void addRoomTest() throws Exception {
        Long id = 1L;
        String name = "101";
        Integer capacity = 12;
        String area = "Building A";

        when(roomService.addRoom(any(Room.class))).thenReturn(new Room(name, capacity, area));

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
        /* mock service result */
        List<Reservation> reservations = List.of(
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2300, 1, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2300, 1, 1, 12, 0, 0, 0)),
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2300, 1, 2, 8, 0, 0, 0),
                        LocalDateTime.of(2300, 1, 2, 9, 0, 0, 0))
        );
        when(roomService.deleteRoom(roomId)).thenReturn(reservations);
        /* perform */
        String responseString = mockMvc.perform(delete("/rooms/{id}", roomId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        /* verify request */
        verify(roomService).deleteRoom(roomId);
        /* verify response */
        DeleteRoomResponse response = objectMapper.readValue(responseString, DeleteRoomResponse.class);
        assertThat(response.roomId()).isEqualTo(roomId);
        assertThat(response.closedReservations())
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservations);
    }

    @Test
    void updateRoomTest() throws Exception {
        Long id = 0L;
        String name = "101";
        Integer capacity = 30;
        String area = "Building A";

        when(roomService.updateRoom(any(Long.class), any(String.class), any(Integer.class), any(String.class)))
                .thenReturn(new Room(name, capacity, area));

        mockMvc.perform(put("/rooms/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new RoomRequest(name, capacity, area))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.capacity").value(capacity))
                .andExpect(jsonPath("$.area").value(area));
    }
}
