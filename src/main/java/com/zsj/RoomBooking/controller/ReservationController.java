package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.model.dto.request.ReservationRequest;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
import com.zsj.RoomBooking.model.dto.request.SearchReservationRequest;
import com.zsj.RoomBooking.model.dto.request.UpdateReservationRequest;
import com.zsj.RoomBooking.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/reservations")
public class ReservationController {
    @Autowired
    private ReservationService service;

    @Autowired
    private ReservationMapper reservationMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReservationResponse> searchReservations(@Valid @ModelAttribute SearchReservationRequest request) {
        return service.searchReservations(request.userId(), request.roomId(), request.date(), request.status())
                .stream().map(reservationMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @reservationAuthorizationService.isOwner(#id, authentication.principal.id)")
    public ReservationResponse getReservation(@PathVariable @Positive Long id) {
        return reservationMapper.toResponse(service.getReservation(id));
    }

    /* Only startTime and endTime are allowed to update */
    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or @reservationAuthorizationService.isOwner(#id, authentication.principal.id)")
    public ReservationResponse updateReservationTime(@PathVariable @Positive Long id, @Valid @RequestBody UpdateReservationRequest request) {
        return reservationMapper.toResponse(service.updateReservationTime(id, request.startTime(), request.endTime()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @reservationAuthorizationService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<Void> deleteReservation(@PathVariable @Positive Long id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

}
