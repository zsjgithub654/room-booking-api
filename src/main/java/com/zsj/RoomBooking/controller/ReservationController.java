package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.model.dto.request.ReservationRequest;
import com.zsj.RoomBooking.model.dto.request.SearchReservationRequest;
import com.zsj.RoomBooking.model.dto.request.UpdateReservationRequest;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
import com.zsj.RoomBooking.security.CustomUserDetails;
import com.zsj.RoomBooking.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
public class ReservationController {
    @Autowired
    private ReservationService service;

    @Autowired
    private ReservationMapper reservationMapper;

    @GetMapping("/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ReservationResponse> searchReservations(@Valid @ModelAttribute SearchReservationRequest request,
                                                        Pageable pageable) {
        return service.searchReservations(
                        request.userId(),
                        request.roomId(),
                        request.date(),
                        request.status(),
                        pageable)
                .map(reservationMapper::toResponse);
    }

    @GetMapping("/users/me/reservations")
    public List<ReservationResponse> getCurrentUserReservations(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return service.searchReservations(
                        customUserDetails.getId(),
                        null,
                        null,
                        null,
                        Pageable.unpaged())
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @GetMapping("/reservations/{id}")
    @PreAuthorize("hasRole('ADMIN') or @reservationAuthorizationService.isOwner(#id, authentication.principal.id)")
    public ReservationResponse getReservation(@PathVariable @Positive Long id) {
        return reservationMapper.toResponse(service.getReservation(id));
    }

    @PostMapping("/users/me/reservations")
    public ResponseEntity<ReservationResponse> addCurrentUserReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody ReservationRequest request) {
        return new ResponseEntity<>(
                reservationMapper.toResponse(
                        service.addReservation(
                                customUserDetails.getId(),
                                request.roomId(),
                                request.startTime(),
                                request.endTime())),
                HttpStatus.CREATED);
    }

    @PostMapping("/users/{id}/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> addReservation(@PathVariable @Positive Long id,
                                                              @Valid @RequestBody ReservationRequest request) {
        return new ResponseEntity<>(
                reservationMapper.toResponse(
                        service.addReservation(
                                id,
                                request.roomId(),
                                request.startTime(),
                                request.endTime())),
                HttpStatus.CREATED);
    }

    /* Only startTime and endTime are allowed to update */
    @PatchMapping(path = "/reservations/{id}")
    @PreAuthorize("hasRole('ADMIN') or @reservationAuthorizationService.isOwner(#id, authentication.principal.id)")
    public ReservationResponse updateReservationTime(@PathVariable @Positive Long id,
                                                     @Valid @RequestBody UpdateReservationRequest request) {
        return reservationMapper.toResponse(
                service.updateReservationTime(
                        id,
                        request.startTime(),
                        request.endTime()));
    }

    @PatchMapping("/reservations/{id}/release")
    @PreAuthorize("hasRole('ADMIN') or @reservationAuthorizationService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<Void> releaseReservation(@PathVariable @Positive Long id) {
        service.releaseReservation(id);
        return ResponseEntity.noContent().build();
    }
}
