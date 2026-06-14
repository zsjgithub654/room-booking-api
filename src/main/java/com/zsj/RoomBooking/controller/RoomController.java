package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.RoomScheduleMapper;
import com.zsj.RoomBooking.mapper.ReservationMapper;
import com.zsj.RoomBooking.mapper.RoomMapper;
import com.zsj.RoomBooking.model.dto.request.SearchRoomRequest;
import com.zsj.RoomBooking.model.dto.request.RoomRequest;
import com.zsj.RoomBooking.model.dto.response.DeleteRoomResponse;
import com.zsj.RoomBooking.model.dto.response.RoomResponse;
import com.zsj.RoomBooking.model.dto.request.SearchAvailabilityRequest;
import com.zsj.RoomBooking.model.dto.response.RoomScheduleResponse;
import com.zsj.RoomBooking.model.criteria.RoomSearchCriteria;
import com.zsj.RoomBooking.service.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private RoomScheduleMapper roomScheduleMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<RoomResponse> searchRooms(@Valid @ModelAttribute SearchRoomRequest request, Pageable pageable) {
        return roomService.searchRooms(
                new RoomSearchCriteria(
                        request.name(),
                        request.minCapacity(),
                        request.maxCapacity(),
                        request.area(),
                        request.status()),
                pageable
        ).map(roomMapper::toResponse);
    }

    @GetMapping("/availabilities")
    public List<RoomScheduleResponse> searchAvailabilities(@Valid @ModelAttribute SearchAvailabilityRequest request) {
        return roomService.searchAvailabilities(
                request.name(),
                request.minCapacity(), request.maxCapacity(),
                request.area(),
                request.fromDate(), request.toDate(),
                request.includeUnavailable()
        ).stream().map(roomScheduleMapper::toResponse).toList();
    }

    @GetMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse getRoom(@PathVariable @Positive Long roomId) {
        return roomMapper.toResponse(roomService.getRoom(roomId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> addRoom(@Valid @RequestBody RoomRequest request) {
        return new ResponseEntity<>(roomMapper.toResponse(
                roomService.addRoom(roomMapper.toEntity(request))),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public DeleteRoomResponse deleteRoom(@PathVariable @Positive Long roomId) {
        return new DeleteRoomResponse(
                roomId,
                roomService.deleteRoom(roomId).stream().map(reservationMapper::toResponse).toList()
        );
    }

    @PutMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse updateRoom(@PathVariable @Positive Long roomId, @Valid @RequestBody RoomRequest request) {
        return roomMapper.toResponse(
                roomService.updateRoom(roomId,
                        request.name(),
                        request.capacity(),
                        request.area(),
                        request.openTime(),
                        request.closeTime())
        );
    }
}
