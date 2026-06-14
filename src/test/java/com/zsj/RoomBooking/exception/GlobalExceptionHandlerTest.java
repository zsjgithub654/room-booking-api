package com.zsj.RoomBooking.exception;

import com.zsj.RoomBooking.model.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundExceptionTest() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Room not found.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Room not found."));
    }

    @Test
    void handleIllegalArgumentAndStateExceptionTest() {
        IllegalStateException exception = new IllegalStateException("Started reservation cannot be updated.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentAndStateException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Started reservation cannot be updated."));
    }

    @Test
    void handleConstraintViolationExceptionTest() {
        ConstraintViolationException exception = new ConstraintViolationException("id must be greater than 0.", null);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("id must be greater than 0."));
    }
}
