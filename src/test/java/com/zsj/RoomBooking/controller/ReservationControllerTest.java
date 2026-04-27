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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        /* request */
        Long userId = 0L;
        Long roomId = 10L;
        LocalDate date = LocalDate.of(2026, 3, 1);
        /* mock service response */
        List<ReservationResponse> serviceResponse = List.of(
                new ReservationResponse(0L, 10L, 20L,
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0),
                        ReservationStatus.RESERVATION_STATUS_ACTIVE),
                new ReservationResponse(1L, 11L, 20L,
                        LocalDateTime.of(2026, 3, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 15, 30, 0, 0),
                        ReservationStatus.RESERVATION_STATUS_CANCELED));
        when(reservationService.searchReservations(any(SearchReservationRequest.class))).thenReturn(serviceResponse);

        /* perform, to compare LocalDateTime, need to parse response to dto object */
        String responseString = mockMvc.perform(get("/reservations")
                        .param("userId", userId.toString())
                        .param("roomId", roomId.toString())
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ReservationResponse> controllerResponse =
                objectMapper.readValue(responseString, new TypeReference<List<ReservationResponse>>() {
                });
        /* verify */
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }

    @Test
    void getReservationTest() throws Exception {
        /* request */
        Long id = 0L;
        /* mock service response */
        ReservationResponse serviceResponse = new ReservationResponse(id, 10L, 100L,
                LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 30, 0, 0),
                ReservationStatus.RESERVATION_STATUS_ACTIVE);
        when(reservationService.getReservation(id)).thenReturn(serviceResponse);
        /* perform */
        String responseString = mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse controllerResponse = objectMapper.readValue(responseString, ReservationResponse.class);
        /* verify */
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }

    @Test
    void addReservationTest() throws Exception {
        /* request */
        Long id = 0L;
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 30, 0, 0);
        /* mock service response */
        ReservationResponse serviceResponse = new ReservationResponse(
                id, userId, roomId, startTime, endTime,
                ReservationStatus.RESERVATION_STATUS_ACTIVE);
        when(reservationService.addReservation(eq(userId), any(ReservationRequest.class))).thenReturn(serviceResponse);
        /* perform */
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
        ReservationResponse controllerResponse = objectMapper.readValue(responseString, ReservationResponse.class);
        /* verify */
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }

    @Test
    void deleteReservationTest() throws Exception {
        /* request */
        Long id = 0L;
        /* mock */
        doNothing().when(reservationService).deleteReservation(eq(id));
        /* perform */
        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateReservationTimeTest() throws Exception {
        /* request */
        Long id = 0L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 1, 11, 30, 0, 0);
        /* mock service response */
        ReservationResponse serviceResponse = new ReservationResponse(id, 10L, 11L,
                startTime, endTime, ReservationStatus.RESERVATION_STATUS_ACTIVE);
        when(reservationService.updateReservationTime(eq(id), any(TimeRangeRequest.class)))
                .thenReturn(serviceResponse);
        /* perform */
        String responseString = mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new TimeRangeRequest(startTime, endTime))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse controllerResponse = objectMapper.readValue(responseString, ReservationResponse.class);
        /* verify */
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }
}
