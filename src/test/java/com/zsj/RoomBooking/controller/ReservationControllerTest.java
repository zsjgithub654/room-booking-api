package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.dto.request.ReservationRequest;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.dto.request.SearchReservationRequest;
import com.zsj.RoomBooking.model.dto.request.TimeRangeRequest;
import com.zsj.RoomBooking.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void searchReservationsTest() throws Exception {
        List<ReservationResponse> serviceResponse = new ArrayList<>();
        serviceResponse.add(new ReservationResponse(0L, 10L, 20L, LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0), LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0), ReservationStatus.RESERVATION_STATUS_ACTIVE));
        serviceResponse.add(new ReservationResponse(1L, 11L, 20L, LocalDateTime.of(2026, 3, 1, 14, 30, 0, 0), LocalDateTime.of(2026, 3, 1, 15, 30, 0, 0), ReservationStatus.RESERVATION_STATUS_CANCELED));
        serviceResponse.add(new ReservationResponse(2L, 11L, 21L, LocalDateTime.of(2026, 3, 3, 10, 0, 0, 0), LocalDateTime.of(2026, 3, 3, 12, 0, 0, 0), ReservationStatus.RESERVATION_STATUS_ACTIVE));

        when(reservationService.searchReservations(any(SearchReservationRequest.class))).thenReturn(serviceResponse);

        /* to compare LocalDateTime, parse response to dto object */
        String responseString = mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ReservationResponse> controllerResponse =
                objectMapper.readValue(responseString, new TypeReference<List<ReservationResponse>>() {});

        for (int i = 0; i < controllerResponse.size(); i++) {
            assertThat(controllerResponse.get(i).id()).isEqualTo(serviceResponse.get(i).id());
            assertThat(controllerResponse.get(i).userId()).isEqualTo(serviceResponse.get(i).userId());
            assertThat(controllerResponse.get(i).roomId()).isEqualTo(serviceResponse.get(i).roomId());
            assertThat(controllerResponse.get(i).startTime()).isEqualTo(serviceResponse.get(i).startTime());
            assertThat(controllerResponse.get(i).endTime()).isEqualTo(serviceResponse.get(i).endTime());
            assertThat(controllerResponse.get(i).reservationStatus()).isEqualTo(serviceResponse.get(i).reservationStatus());
        }
    }

    @Test
    void getReservationTest() throws Exception {
        Long id = 1L;
        Long userId = 11L;
        Long roomId = 12L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 30, 0, 0);
        ReservationStatus status = ReservationStatus.RESERVATION_STATUS_ACTIVE;

        when(reservationService.getReservation(id))
                .thenReturn(new ReservationResponse(id, userId, roomId, startTime, endTime, status));

        String responseString = mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse response = objectMapper.readValue(responseString, ReservationResponse.class);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.roomId()).isEqualTo(roomId);
        assertThat(response.startTime()).isEqualTo(startTime);
        assertThat(response.endTime()).isEqualTo(endTime);
        assertThat(response.reservationStatus()).isEqualTo(status);
    }

    @Test
    void addReservationTest() throws Exception {
        Long id = 0L;
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 30, 0, 0);

        when(reservationService.addReservation(eq(userId), any(ReservationRequest.class)))
                .thenReturn(new ReservationResponse(
                        id, userId, roomId, startTime, endTime,
                        ReservationStatus.RESERVATION_STATUS_ACTIVE));

        String responseString = mockMvc.perform(post("/reservations")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                // HTTP 201 Created is the successful status of post request
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse response = objectMapper.readValue(responseString, ReservationResponse.class);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.roomId()).isEqualTo(roomId);
        assertThat(response.startTime()).isEqualTo(startTime);
        assertThat(response.endTime()).isEqualTo(endTime);
        assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
    }

    @Test
    void deleteReservationTest() throws Exception {
        Long id = 0L;

        doNothing().when(reservationService).deleteReservation(eq(id));

        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateReservationTimeTest() throws Exception {
        Long id = 0L;
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 30, 0, 0);

        when(reservationService.updateReservationTime(eq(id), any(TimeRangeRequest.class)))
                .thenReturn(new ReservationResponse(id, userId, roomId, startTime, endTime,
                ReservationStatus.RESERVATION_STATUS_ACTIVE));

        String responseString = mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new TimeRangeRequest(startTime, endTime))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse response = objectMapper.readValue(responseString, ReservationResponse.class);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.roomId()).isEqualTo(roomId);
        assertThat(response.startTime()).isEqualTo(startTime);
        assertThat(response.endTime()).isEqualTo(endTime);
        assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.RESERVATION_STATUS_ACTIVE);
    }
}
