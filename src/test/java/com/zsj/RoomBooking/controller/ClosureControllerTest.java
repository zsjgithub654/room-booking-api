package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.dto.response.ClosureResponse;
import com.zsj.RoomBooking.service.ClosureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClosureController.class)
public class ClosureControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClosureService closureService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getClosureTest() throws Exception {
        /* request */
        Long closureId = 0L;
        /* mock service response */
        ClosureResponse serviceResponse = new ClosureResponse(closureId, 10L, 100L,
                LocalDateTime.of(2026, 3, 1, 10, 30, 0, 0),
                LocalDateTime.of(2026, 3, 2, 10, 30, 0, 0));
        when(closureService.getClosure(eq(closureId))).thenReturn(serviceResponse);
        /* perform */
        String responseString = mockMvc.perform(get("/closures/{closureId}", closureId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ClosureResponse controllerResponse = objectMapper.readValue(responseString, ClosureResponse.class);
        /* verify */
        assertThat(controllerResponse)
                .usingRecursiveComparison()
                .isEqualTo(serviceResponse);
    }

    @Test
    void deleteClosureTest() throws Exception {
        Long closureId = 3L;

        doNothing().when(closureService).deleteClosure(eq(closureId));

        mockMvc.perform(delete("/closures/{closureId}", closureId))
                /* deleted status code 204 */
                .andExpect(status().isNoContent());
    }
}