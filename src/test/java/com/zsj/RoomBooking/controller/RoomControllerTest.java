package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.config.SecurityConfig;
import com.zsj.RoomBooking.mapper.OccupationMapper;
import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.mapper.RoomMapper;
import com.zsj.RoomBooking.mapper.RoomScheduleMapper;
import com.zsj.RoomBooking.model.result.RoomSchedule;
import com.zsj.RoomBooking.model.RoomStatus;
import com.zsj.RoomBooking.model.dto.request.RoomRequest;
import com.zsj.RoomBooking.model.dto.request.SearchAvailabilityRequest;
import com.zsj.RoomBooking.model.dto.request.SearchRoomRequest;
import com.zsj.RoomBooking.model.dto.response.DeleteRoomResponse;
import com.zsj.RoomBooking.model.dto.response.RoomResponse;
import com.zsj.RoomBooking.model.dto.response.RoomScheduleResponse;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.model.criteria.RoomSearchCriteria;
import com.zsj.RoomBooking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(RoomController.class)
@Import({SecurityConfig.class,
        RoomMapper.class,
        ReservationMapper.class,
        RoomScheduleMapper.class,
        OccupationMapper.class,
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
                null,
                RoomStatus.ROOM_STATUS_DELETED);
        RoomSearchCriteria criteria = new RoomSearchCriteria(
                request.name(),
                request.minCapacity(),
                request.maxCapacity(),
                request.area(),
                request.status());
        /* mock service result */
        List<Room> rooms = List.of(
                new Room("101", 12, "Building-A", null, null),
                new Room("102", 4, "Building-A", null, null),
                new Room("101", 6, "Building-B", null, null)
        );
        when(roomService.searchRooms(
                eq(criteria),
                eq(PageRequest.of(0, 20))
        )).thenReturn(new PageImpl<>(rooms, PageRequest.of(0, 20), rooms.size()));
        /* perform */
        String responseString = mockMvc.perform(get("/rooms")
                        .with(user("admin1").roles("ADMIN"))
                        .param("minCapacity", request.minCapacity().toString())
                        .param("maxCapacity", request.maxCapacity().toString())
                        .param("status", request.status().toString())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        /* verify params passed to service */
        verify(roomService).searchRooms(criteria, PageRequest.of(0, 20));
        TypeReference<List<RoomResponse>> typeReference = new TypeReference<List<RoomResponse>>() {
        };
        String contentString = objectMapper.readTree(responseString).get("content").toString();
        List<RoomResponse> responses = objectMapper.readValue(contentString, typeReference);
        assertThat(responses)
                .usingRecursiveComparison()
                .isEqualTo(rooms);
    }

    @Test
    void searchRoomsShouldRejectInvalidCapacityRange() throws Exception {
        mockMvc.perform(get("/rooms")
                        .with(user("admin1").roles("ADMIN"))
                        .param("minCapacity", "20")
                        .param("maxCapacity", "2"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomService);
    }

    @Test
    void searchRoomsShouldRejectNonAdmin() throws Exception {
        mockMvc.perform(get("/rooms"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(roomService);
    }

    @Test
    void searchAvailabilitiesTest() throws Exception {
        /* request */
        SearchAvailabilityRequest request = new SearchAvailabilityRequest(null,
                2, 20,
                null,
                LocalDate.of(2300, 3, 1),
                LocalDate.of(2300, 3, 2),
                true);
        /* mock service result */
        List<RoomSchedule> roomSchedules = List.of(
                new RoomSchedule(new Room("101", 12, "Building-A", null, null),
                        List.of(
                                new Reservation(new User(), new Room(),
                                        LocalDateTime.of(2026, 3, 1, 11, 0, 0, 0),
                                        LocalDateTime.of(2026, 3, 1, 12, 0, 0, 0)),
                                new Closure(new Room(),
                                        LocalDateTime.of(2026, 3, 1, 13, 0, 0, 0),
                                        LocalDateTime.of(2026, 3, 1, 17, 0, 0, 0))
                        )
                ),
                new RoomSchedule(new Room("102", 4, "Building-B", null, null),
                        List.of(
                                new Closure(new Room(),
                                        LocalDateTime.of(2026, 3, 2, 8, 0, 0, 0),
                                        LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0))
                        )
                ));
        when(roomService.searchAvailabilities(
                request.name(),
                request.minCapacity(), request.maxCapacity(),
                request.area(),
                request.fromDate(), request.toDate(),
                request.includeUnavailable())
        ).thenReturn(roomSchedules);
        /* perform */
        String responseString = mockMvc.perform(get("/rooms/availabilities")
                        .param("minCapacity", request.minCapacity().toString())
                        .param("maxCapacity", request.maxCapacity().toString())
                        .param("fromDate", request.fromDate().toString())
                        .param("toDate", request.toDate().toString())
                        .param("includeUnavailable", request.includeUnavailable().toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<RoomScheduleResponse> responses =
                objectMapper.readValue(responseString, new TypeReference<List<RoomScheduleResponse>>() {
                });
        /* verify response */
        assertThat(responses)
                .usingRecursiveComparison()
                .ignoringFields("occupations.type")
                .isEqualTo(roomSchedules);
    }

    @Test
    void searchAvailabilitiesShouldRejectInvalidTimeRangeTest() throws Exception {
        mockMvc.perform(get("/rooms/availabilities")
                        .param("fromDate", LocalDate.of(2300, 3, 2).toString())
                        .param("toDate", LocalDate.of(2300, 3, 1).toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomService);
    }

    @Test
    void searchAvailabilitiesShouldRejectMissingTimeRangeTest() throws Exception {
        mockMvc.perform(get("/rooms/availabilities"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomService);
    }

    @Test
    void searchAvailabilitiesShouldValidateTimeRangeLengthTest() throws Exception {
        LocalDate fromDate = LocalDate.of(2300, 3, 1);
        LocalDate validToDate = LocalDate.of(2300, 3, 7);

        when(roomService.searchAvailabilities(
                null,
                null, null,
                null,
                fromDate, validToDate,
                null))
                .thenReturn(List.of());

        mockMvc.perform(get("/rooms/availabilities")
                        .param("fromDate", fromDate.toString())
                        .param("toDate", validToDate.toString()))
                .andExpect(status().isOk());

        verify(roomService).searchAvailabilities(
                null,
                null, null,
                null,
                fromDate, validToDate,
                null);

        mockMvc.perform(get("/rooms/availabilities")
                        .param("fromDate", fromDate.toString())
                        .param("toDate", LocalDate.of(2300, 3, 8).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRoomTest() throws Exception {
        /* mock service result */
        Long id = 1L;
        Room room = new Room("101", 12, "Building-A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));
        when(roomService.getRoom(id)).thenReturn(room);
        /* perform and verify */
        String responseString = mockMvc.perform(get("/rooms/{id}", id)
                        .with(user("admin1").roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        RoomResponse response = objectMapper.readValue(responseString, RoomResponse.class);
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(room);
    }

    @Test
    void getRoomShouldRejectNonPositiveId() throws Exception {
        mockMvc.perform(get("/rooms/{id}", 0)
                        .with(user("admin1").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomService);
    }

    @Test
    void addRoomTest() throws Exception {
        Room room = new Room("101", 12, "Building-A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));

        when(roomService.addRoom(any(Room.class))).thenReturn(room);

        String responseString = mockMvc.perform(post("/rooms")
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new RoomRequest(
                                room.getName(),
                                room.getCapacity(),
                                room.getArea(),
                                room.getOpenTime(), room.getCloseTime()
                        ))))
                // HTTP 201 Created is the successful status of post request
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        RoomResponse response = objectMapper.readValue(responseString, RoomResponse.class);
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(room);
    }

    @Test
    void addRoomShouldAcceptOpenAllDayByOmittingHours() throws Exception {
        Room room = new Room("101", 12, "Building-A", null, null);

        when(roomService.addRoom(any(Room.class))).thenReturn(room);

        String responseString = mockMvc.perform(post("/rooms")
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new RoomRequest(
                                room.getName(),
                                room.getCapacity(),
                                room.getArea(),
                                null, null
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        RoomResponse response = objectMapper.readValue(responseString, RoomResponse.class);
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(room);
    }

    @Test
    void addRoomShouldRejectInvalidOperatingHours() throws Exception {
        RoomRequest request = new RoomRequest("101", 12, "Building-A",
                LocalTime.of(16, 0, 0, 0),
                LocalTime.of(9, 0, 0, 0));

        mockMvc.perform(post("/rooms")
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomService);
    }

    @Test
    void addRoomShouldRejectOneSidedOperatingHours() throws Exception {
        RoomRequest request = new RoomRequest("101", 12, "Building-A",
                LocalTime.of(9, 0, 0, 0),
                null);

        mockMvc.perform(post("/rooms")
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomService);
    }

    @Test
    void addRoomShouldRejectBlankName() throws Exception {
        RoomRequest request = new RoomRequest("   ", 12, "Building-A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));

        mockMvc.perform(post("/rooms")
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomService);
    }

    @Test
    void addRoomShouldRejectBlankArea() throws Exception {
        RoomRequest request = new RoomRequest("101", 12, "   ",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));

        mockMvc.perform(post("/rooms")
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomService);
    }

    @Test
    void addRoomShouldRejectNonPositiveCapacity() throws Exception {
        RoomRequest request = new RoomRequest("101", 0, "Building-A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));

        mockMvc.perform(post("/rooms")
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roomService);
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
        String responseString = mockMvc.perform(delete("/rooms/{id}", roomId)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN")))
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
        Long id = 1L;
        Room room = new Room("101", 12, "Building-A",
                LocalTime.of(9, 0, 0, 0),
                LocalTime.of(16, 0, 0, 0));

        when(roomService.updateRoom(id,
                room.getName(),
                room.getCapacity(),
                room.getArea(),
                room.getOpenTime(), room.getCloseTime()))
                .thenReturn(room);

        String responseString = mockMvc.perform(put("/rooms/{id}", id)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new RoomRequest(
                                room.getName(),
                                room.getCapacity(),
                                room.getArea(),
                                room.getOpenTime(), room.getCloseTime()
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        RoomResponse response = objectMapper.readValue(responseString, RoomResponse.class);
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(room);
    }

    @Test
    void updateRoomShouldAcceptOpenAllDayByOmittingHours() throws Exception {
        Long id = 1L;
        Room room = new Room("101", 12, "Building-A", null, null);

        when(roomService.updateRoom(id,
                room.getName(),
                room.getCapacity(),
                room.getArea(),
                null, null))
                .thenReturn(room);

        String responseString = mockMvc.perform(put("/rooms/{id}", id)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new RoomRequest(
                                room.getName(),
                                room.getCapacity(),
                                room.getArea(),
                                null, null
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        RoomResponse response = objectMapper.readValue(responseString, RoomResponse.class);
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(room);
    }
}
