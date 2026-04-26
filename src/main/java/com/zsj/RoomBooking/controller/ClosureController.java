package com.zsj.RoomBooking.controller;

import com.zsj.RoomBooking.model.dto.response.ClosureResponse;
import com.zsj.RoomBooking.service.ClosureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/closures")
public class ClosureController {
    @Autowired
    private ClosureService closureService;

    @GetMapping("/{closureId}")
    public ClosureResponse getClosure(@PathVariable Long closureId) {
        return closureService.getClosure(closureId);
    }

    @DeleteMapping("/{closureId}")
    public ResponseEntity<Void> deleteClosure(@PathVariable Long closureId) {
        closureService.deleteClosure(closureId);
        return ResponseEntity.noContent().build();
    }

}
