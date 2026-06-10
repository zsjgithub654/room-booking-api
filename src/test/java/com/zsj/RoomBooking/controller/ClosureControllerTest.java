package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.AddClosureMapper;
import com.zsj.RoomBooking.mapper.ClosureMapper;
import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.model.AddClosureResult;
import com.zsj.RoomBooking.model.dto.request.ClosureRequest;
import com.zsj.RoomBooking.model.dto.response.AddClosureResponse;
import com.zsj.RoomBooking.model.dto.response.ClosureResponse;
import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.entity.Reservation;
import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.User;
import com.zsj.RoomBooking.service.ClosureService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Enable after closure controller security coverage is added.")
@AutoConfigureMockMvc
@WebMvcTest(ClosureController.class)
@Import({
        ClosureMapper.class,
        AddClosureMapper.class,
        ReservationMapper.class
})
public class ClosureControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClosureService closureService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getClosureTest() throws Exception {
        /* request */
        Long closureId = 1L;
        /* mock service response */
        Closure closure = new Closure(new Room(),
                LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0),
                LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0));
        when(closureService.getClosure(eq(closureId))).thenReturn(closure);
        /* perform */
        String responseString = mockMvc.perform(get("/closures/{closureId}", closureId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ClosureResponse controllerResponse = objectMapper.readValue(responseString, ClosureResponse.class);
        /* verify */
        verify(closureService).getClosure(closureId);
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(closure);
    }

    @Test
    void getClosureShouldRejectNonPositiveId() throws Exception {
        mockMvc.perform(get("/closures/{closureId}", 0))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(closureService);
    }

    @Test
    void deleteClosureTest() throws Exception {
        Long closureId = 3L;

        doNothing().when(closureService).deleteClosure(eq(closureId));

        mockMvc.perform(delete("/closures/{closureId}", closureId))
                /* deleted status code 204 */
                .andExpect(status().isNoContent());
        verify(closureService).deleteClosure(closureId);
    }

    @Test
    void getClosuresOfRoomTest() throws Exception {
        /* request */
        Long roomId = 2L;
        /* mock service result */
        List<Closure> closures = List.of(
                new Closure(new Room(), LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0), LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0)),
                new Closure(new Room(), LocalDateTime.of(2026, 3, 3, 8, 30, 0, 0), LocalDateTime.of(2026, 3, 3, 12, 0, 0, 0)));
        when(closureService.getClosuresOfRoom(eq(roomId))).thenReturn(closures);
        /* perform, to compare LocalDateTime, parse response to dto object */
        String responseString = mockMvc.perform(get("/closures")
                        .param("roomId", roomId.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ClosureResponse> controllerResponse =
                objectMapper.readValue(responseString, new TypeReference<List<ClosureResponse>>() {
                });
        /* verify */
        verify(closureService).getClosuresOfRoom(roomId);
        Assertions.assertThat(controllerResponse)
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(closures);
    }

    @Test
    void getClosuresOfRoomShouldRejectNonPositiveRoomId() throws Exception {
        mockMvc.perform(get("/closures")
                        .param("roomId", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(closureService);
    }

    @Test
    void addClosureTest() throws Exception {
        /* request */
        Long userId = 1L;
        Long roomId = 2L;
        LocalDateTime startTime = LocalDateTime.of(2300, 1, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 1, 10, 10, 30, 0, 0);
        /* mock service result */
        AddClosureResult addClosureResult = new AddClosureResult(
                new Closure(new Room(), startTime, endTime),
                List.of(
                        new Reservation(new User(), new Room(),
                                LocalDateTime.of(2300, 1, 1, 10, 0, 0, 0),
                                LocalDateTime.of(2300, 1, 1, 12, 0, 0, 0)),
                        new Reservation(new User(), new Room(),
                                LocalDateTime.of(2300, 1, 2, 8, 0, 0, 0),
                                LocalDateTime.of(2300, 1, 2, 9, 0, 0, 0))
                ));
        when(closureService.addClosure(eq(roomId), eq(userId), eq(startTime), eq(endTime))).thenReturn(addClosureResult);

        /* perform, need to parse response to dto object to compare LocalDateTime */
        String responseString = mockMvc.perform(post("/closures")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new ClosureRequest(roomId, startTime, endTime))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        AddClosureResponse response = objectMapper.readValue(responseString, AddClosureResponse.class);
        /* verify */
        verify(closureService).addClosure(roomId, userId, startTime, endTime);
        Assertions.assertThat(response.closure())
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(addClosureResult.getClosure());
        Assertions.assertThat(response.canceledReservations())
                .usingRecursiveComparison()
                .ignoringFields("userId", "roomId")
                .isEqualTo(addClosureResult.getCanceledReservations());
    }

    @Test
    void addClosureShouldRejectInvalidTimeRange() throws Exception {
        Long userId = 1L;
        Long roomId = 2L;
        LocalDateTime startTime = LocalDateTime.of(2300, 1, 10, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 1, 1, 10, 30, 0, 0);

        mockMvc.perform(post("/closures")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new ClosureRequest(roomId, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(closureService);
    }

    @Test
    void addClosureShouldRejectNullRoomId() throws Exception {
        Long userId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2300, 1, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 1, 10, 10, 30, 0, 0);

        mockMvc.perform(post("/closures")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new ClosureRequest(null, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(closureService);
    }

    @Test
    void addClosureShouldRejectNonPositiveRoomId() throws Exception {
        Long userId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2300, 1, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 1, 10, 10, 30, 0, 0);

        mockMvc.perform(post("/closures")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new ClosureRequest(0L, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(closureService);
    }

    @Test
    void addClosureShouldRejectNonPositiveUserId() throws Exception {
        Long roomId = 2L;
        LocalDateTime startTime = LocalDateTime.of(2300, 1, 1, 10, 30, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 1, 10, 10, 30, 0, 0);

        mockMvc.perform(post("/closures")
                        .param("userId", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new ClosureRequest(roomId, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(closureService);
    }

    @Test
    void addClosureShouldRejectSecondPrecisionTime() throws Exception {
        Long userId = 1L;
        Long roomId = 2L;
        LocalDateTime startTime = LocalDateTime.of(2300, 1, 1, 10, 30, 1, 0);
        LocalDateTime endTime = LocalDateTime.of(2300, 1, 10, 10, 30, 0, 0);

        mockMvc.perform(post("/closures")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(new ClosureRequest(roomId, startTime, endTime))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(closureService);
    }
}
