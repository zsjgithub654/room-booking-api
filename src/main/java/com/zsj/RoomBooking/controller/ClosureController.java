package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.mapper.AddClosureMapper;
import com.zsj.RoomBooking.mapper.ClosureMapper;
import com.zsj.RoomBooking.model.dto.request.ClosureRequest;
import com.zsj.RoomBooking.model.dto.response.AddClosureResponse;
import com.zsj.RoomBooking.model.dto.response.ClosureResponse;
import com.zsj.RoomBooking.service.ClosureService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
public class ClosureController {
    @Autowired
    private ClosureService closureService;

    @Autowired
    private ClosureMapper closureMapper;

    @Autowired
    private AddClosureMapper addClosureMapper;

    @GetMapping("/closures/{closureId}")
    public ClosureResponse getClosure(@PathVariable @Positive Long closureId) {
        return closureMapper.toResponse(closureService.getClosure(closureId));
    }

    @DeleteMapping("/closures/{closureId}")
    public ResponseEntity<Void> deleteClosure(@PathVariable @Positive Long closureId) {
        closureService.deleteClosure(closureId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/{roomId}/closures")
    public List<ClosureResponse> getClosuresOfRoom(@PathVariable @Positive Long roomId) {
        return closureService.getClosuresOfRoom(roomId)
                .stream()
                .map(closureMapper::toResponse)
                .toList();
    }

    @PostMapping("/rooms/{roomId}/closures")
    public ResponseEntity<AddClosureResponse> addClosure(@PathVariable @Positive Long roomId,
                                                         @Valid @RequestBody ClosureRequest request) {
        return new ResponseEntity<>(
                addClosureMapper.toResponse(
                        closureService.addClosure(
                                roomId,
                                request.startTime(),
                                request.endTime())),
                HttpStatus.CREATED);
    }
}
