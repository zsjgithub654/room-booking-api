package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.dto.request.ReservationRequest;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
import com.zsj.RoomBooking.model.dto.request.SearchReservationRequest;
import com.zsj.RoomBooking.model.dto.request.TimeRangeRequest;
import com.zsj.RoomBooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    @Autowired
    private ReservationService service;

    @GetMapping
    public List<ReservationResponse> searchReservations(@ModelAttribute SearchReservationRequest request) {
        return service.searchReservations(request);
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable Long id) {
        return service.getReservation(id);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@RequestParam Long userId, @RequestBody ReservationRequest reservationRequest) {
        return new ResponseEntity<>(service.addReservation(userId, reservationRequest), HttpStatus.CREATED);
    }

    /* Only startTime and endTime are allowed to update */
    @PutMapping("/{id}")
    public ReservationResponse updateReservationTime(@PathVariable Long id, @RequestBody TimeRangeRequest request) {
        return service.updateReservationTime(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

}
