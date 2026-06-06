package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.model.dto.request.ReservationRequest;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
import com.zsj.RoomBooking.model.dto.request.SearchReservationRequest;
import com.zsj.RoomBooking.model.dto.request.UpdateReservationRequest;
import com.zsj.RoomBooking.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Autowired
    private ReservationMapper reservationMapper;

    @GetMapping
    public List<ReservationResponse> searchReservations(@ModelAttribute SearchReservationRequest request) {
        return service.searchReservations(request.userId(), request.roomId(), request.date(), request.status())
                .stream().map(reservationMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable Long id) {
        return reservationMapper.toResponse(service.getReservation(id));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@RequestParam Long userId, @Valid @RequestBody ReservationRequest request) {
        return new ResponseEntity<>(
                reservationMapper.toResponse(service.addReservation(
                        userId, request.roomId(), request.startTime(), request.endTime())),
                HttpStatus.CREATED);
    }

    /* Only startTime and endTime are allowed to update */
    @PatchMapping(path = "/{id}")
    public ReservationResponse updateReservationTime(@PathVariable Long id, @Valid @RequestBody UpdateReservationRequest request) {
        return reservationMapper.toResponse(service.updateReservationTime(id, request.startTime(), request.endTime()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

}
