package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.model.ReservationStatus;
import com.zsj.RoomBooking.model.dto.request.ReservationRequest;
import com.zsj.RoomBooking.model.dto.request.UpdateReservationRequest;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(ReservationMapper.class)
public class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void searchReservationsTest() throws Exception {
        /* request */
        Long userId = 1L;
        Long roomId = 10L;
        LocalDate date = LocalDate.of(2026, 3, 1);
        ReservationStatus status = ReservationStatus.RESERVATION_STATUS_ACTIVE;
        /* mock service response */
        List<Reservation> reservations = List.of(
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0)),
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2026, 3, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 15, 30, 0, 0)));
        when(reservationService.searchReservations(eq(userId), eq(roomId), eq(date), eq(status))).thenReturn(reservations);

        /* perform, to compare LocalDateTime, need to parse response to dto object */
        String responseString = mockMvc.perform(get("/reservations")
                        .param("userId", userId.toString())
                        .param("roomId", roomId.toString())
                        .param("date", date.toString())
                        .param("status", status.name()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ReservationResponse> responses =
                objectMapper.readValue(responseString, new TypeReference<List<ReservationResponse>>() {
                });
        /* verify */
        verify(reservationService).searchReservations(userId, roomId, date, status);
        assertThat(responses)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservations);
    }

    @Test
    void searchReservationsShouldRejectNonPositiveRoomId() throws Exception {
        mockMvc.perform(get("/reservations")
                        .param("roomId", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void getReservationTest() throws Exception {
        /* request */
        Long id = 1L;
        /* mock service response */
        Reservation reservation = new Reservation(new User(), new Room(),
                LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 30, 0, 0));
        when(reservationService.getReservation(id)).thenReturn(reservation);
        /* perform */
        String responseString = mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse response = objectMapper.readValue(responseString, ReservationResponse.class);
        /* verify */
        verify(reservationService).getReservation(id);
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservation);
    }

    @Test
    void getReservationShouldRejectNonPositiveId() throws Exception {
        mockMvc.perform(get("/reservations/{id}", 0))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservationTest() throws Exception {
        /* request */
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);
        /* mock service response */
        Reservation reservation = new Reservation(new User(), new Room(), startTime, endTime);
        when(reservationService.addReservation(eq(userId), eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(reservation);
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
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservation);
    }

    @Test
    void addReservationShouldRejectInvalidTimeRange() throws Exception {
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);

        mockMvc.perform(post("/reservations")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservationShouldRejectPastStartTime() throws Exception {
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(post("/reservations")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservationShouldRejectNullRoomId() throws Exception {
        Long userId = 10L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(post("/reservations")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(null, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservationShouldRejectNonPositiveRoomId() throws Exception {
        Long userId = 10L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(post("/reservations")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(0L, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservationShouldRejectNonPositiveUserId() throws Exception {
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(post("/reservations")
                        .param("userId", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservationShouldRejectSecondPrecisionTime() throws Exception {
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 1, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(post("/reservations")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void deleteReservationTest() throws Exception {
        /* request */
        Long id = 1L;
        /* mock */
        doNothing().when(reservationService).deleteReservation(eq(id));
        /* perform */
        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNoContent());
        verify(reservationService).deleteReservation(id);
    }

    @Test
    void updateReservationTimeTest() throws Exception {
        /* request */
        Long id = 1L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);
        /* mock service response */
        Reservation reservation = new Reservation(new User(), new Room(), startTime, endTime);
        when(reservationService.updateReservationTime(eq(id), eq(startTime), eq(endTime))).thenReturn(reservation);
        /* perform */
        String responseString = mockMvc.perform(patch("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateReservationRequest(startTime, endTime))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse response = objectMapper.readValue(responseString, ReservationResponse.class);
        /* verify */
        verify(reservationService).updateReservationTime(id, startTime, endTime);
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservation);
    }

    @Test
    void updateReservationTimeShouldRejectSecondPrecisionTime() throws Exception {
        Long id = 1L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 30, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(patch("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateReservationRequest(startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }
}
