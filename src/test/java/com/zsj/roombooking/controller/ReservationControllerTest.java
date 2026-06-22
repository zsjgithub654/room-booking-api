package com.zsj.roombooking.controller;

import com.zsj.roombooking.config.SecurityConfig;
import com.zsj.roombooking.mapper.ReservationMapper;
import com.zsj.roombooking.model.Role;
import com.zsj.roombooking.model.ReservationStatus;
import com.zsj.roombooking.model.dto.request.ReservationRequest;
import com.zsj.roombooking.model.dto.request.UpdateReservationRequest;
import com.zsj.roombooking.model.dto.response.ReservationResponse;
import com.zsj.roombooking.model.entity.Reservation;
import com.zsj.roombooking.model.entity.Room;
import com.zsj.roombooking.model.entity.User;
import com.zsj.roombooking.security.CustomUserDetails;
import com.zsj.roombooking.security.ReservationAuthorizationService;
import com.zsj.roombooking.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(ReservationController.class)
@Import({ReservationMapper.class, SecurityConfig.class})
public class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ReservationAuthorizationService reservationAuthorizationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void searchReservationsTest() throws Exception {
        /* request */
        Long userId = 1L;
        Long roomId = 10L;
        LocalDate date = LocalDate.of(2026, 3, 1);
        ReservationStatus status = ReservationStatus.RESERVATION_STATUS_SCHEDULED;
        /* mock service response */
        List<Reservation> reservations = List.of(
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0)),
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2026, 3, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 15, 30, 0, 0)));
        when(reservationService.searchReservations(eq(userId), eq(roomId), eq(date), eq(status), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(reservations, PageRequest.of(0, 20), reservations.size()));

        String responseString = mockMvc.perform(get("/reservations")
                        .with(user("admin1").roles("ADMIN"))
                        .param("userId", userId.toString())
                        .param("roomId", roomId.toString())
                        .param("date", date.toString())
                        .param("status", status.name())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(reservationService).searchReservations(userId, roomId, date, status, PageRequest.of(0, 20));
        TypeReference<List<ReservationResponse>> typeReference = new TypeReference<List<ReservationResponse>>() {
        };
        String contentString = objectMapper.readTree(responseString).get("content").toString();
        List<ReservationResponse> responses = objectMapper.readValue(contentString, typeReference);
        assertThat(responses)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservations);
    }

    @Test
    void searchReservationsShouldRejectNonAdmin() throws Exception {
        mockMvc.perform(get("/reservations")
                        .with(authentication(getAuthentication(1L, "user1"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reservationService);
    }


    @Test
    void searchReservationsShouldRejectNonPositiveRoomId() throws Exception {
        mockMvc.perform(get("/reservations")
                        .with(user("admin1").roles("ADMIN"))
                        .param("roomId", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void searchReservationsShouldRejectInvalidSortProperty() throws Exception {
        mockMvc.perform(get("/reservations")
                        .with(user("admin1").roles("ADMIN"))
                        .param("sort", "[\"\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sorting parameters."));

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
        String responseString = mockMvc.perform(get("/reservations/{id}", id)
                        .with(user("admin1").roles("ADMIN")))
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
        mockMvc.perform(get("/reservations/{id}", 0)
                        .with(user("admin1").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void getCurrentUserReservationsTest() throws Exception {
        Long userId = 1L;
        String username = "user1";
        List<Reservation> reservations = List.of(
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0)),
                new Reservation(new User(), new Room(),
                        LocalDateTime.of(2026, 3, 1, 14, 30, 0, 0),
                        LocalDateTime.of(2026, 3, 1, 15, 30, 0, 0)));
        when(reservationService.searchReservations(eq(userId), eq(null), eq(null), eq(null), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(reservations));

        String responseString = mockMvc.perform(get("/users/me/reservations")
                        .with(authentication(getAuthentication(userId, username))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ReservationResponse> responses =
                objectMapper.readValue(responseString, new TypeReference<List<ReservationResponse>>() {
                });

        verify(reservationService).searchReservations(userId, null, null, null, Pageable.unpaged());
        assertThat(responses)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservations);
    }

    @Test
    void getCurrentUserReservationsShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/users/me/reservations"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservationTest() throws Exception {
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);
        Reservation reservation = new Reservation(new User(), new Room(), startTime, endTime);
        when(reservationService.addReservation(eq(userId), eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(reservation);

        String responseString = mockMvc.perform(post("/users/{id}/reservations", userId)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse response = objectMapper.readValue(responseString, ReservationResponse.class);

        verify(reservationService).addReservation(userId, roomId, startTime, endTime);
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservation);
    }

    @Test
    void addReservationShouldRejectNonAdmin() throws Exception {
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(post("/users/{id}/reservations", userId)
                        .with(csrf())
                        .with(authentication(getAuthentication(1L, "user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reservationService);
    }

    @Test
    void addCurrentUserReservationTest() throws Exception {
        Long userId = 10L;
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);
        Reservation reservation = new Reservation(new User(), new Room(), startTime, endTime);
        when(reservationService.addReservation(eq(userId), eq(roomId), eq(startTime), eq(endTime)))
                .thenReturn(reservation);

        String responseString = mockMvc.perform(post("/users/me/reservations")
                        .with(csrf())
                        .with(authentication(getAuthentication(userId, "user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ReservationResponse response = objectMapper.readValue(responseString, ReservationResponse.class);

        verify(reservationService).addReservation(userId, roomId, startTime, endTime);
        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(reservation);
    }

    @Test
    void addCurrentUserReservationShouldRequireAuthentication() throws Exception {
        Long roomId = 11L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        mockMvc.perform(post("/users/me/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper
                                .writeValueAsString(new ReservationRequest(roomId, startTime, endTime))))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reservationService);
    }

    @Test
    void releaseReservationTest() throws Exception {
        /* request */
        Long id = 1L;
        /* mock */
        doNothing().when(reservationService).releaseReservation(eq(id));
        /* perform */
        mockMvc.perform(patch("/reservations/{id}/release", id)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN")))
                .andExpect(status().isNoContent());
        verify(reservationService).releaseReservation(id);
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
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
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
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateReservationRequest(startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    void updateReservationShouldRejectWhenCanceledTest() throws Exception {
        Long id = 1L;
        LocalDateTime startTime = LocalDateTime.of(2300, 3, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 3, 1, 11, 30, 0, 0);

        when(reservationService.updateReservationTime(eq(id), eq(startTime), eq(endTime)))
                .thenThrow(new IllegalStateException("Cannot update a canceled reservation."));

        mockMvc.perform(patch("/reservations/{id}", id)
                        .with(csrf())
                        .with(user("admin1").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new UpdateReservationRequest(startTime, endTime))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot update a canceled reservation."));
    }

    private UsernamePasswordAuthenticationToken getAuthentication(Long userId, String username) {
        CustomUserDetails customUserDetails = new CustomUserDetails(userId, username, "password", Set.of(Role.ROLE_USER), true);
        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }
}
