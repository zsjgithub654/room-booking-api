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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/closures")
public class ClosureController {
    @Autowired
    private ClosureService closureService;

    @Autowired
    private ClosureMapper closureMapper;

    @Autowired
    private AddClosureMapper addClosureMapper;

    @GetMapping("/{closureId}")
    public ClosureResponse getClosure(@PathVariable @Positive Long closureId) {
        return closureMapper.toResponse(closureService.getClosure(closureId));
    }

    @DeleteMapping("/{closureId}")
    public ResponseEntity<Void> deleteClosure(@PathVariable @Positive Long closureId) {
        closureService.deleteClosure(closureId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<ClosureResponse> getClosuresOfRoom(@RequestParam @Positive Long roomId) {
        return closureService.getClosuresOfRoom(roomId).stream().map(closureMapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<AddClosureResponse> addClosure(
            @RequestParam @Positive Long userId, @Valid @RequestBody ClosureRequest request) {
        return new ResponseEntity<>(
                addClosureMapper.toResponse(
                        closureService.addClosure(request.roomId(), userId, request.startTime(), request.endTime())),
                HttpStatus.CREATED
        );
    }
}
