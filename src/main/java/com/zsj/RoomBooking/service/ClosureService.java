package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.AddClosureResult;
import com.zsj.RoomBooking.model.entity.Closure;

import java.time.LocalDateTime;
import java.util.List;

public interface ClosureService {
    Closure getClosure(Long Id);
    List<Closure> getClosuresOfRoom(Long roomId);
    AddClosureResult addClosure(Long roomId, Long userId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteClosure(Long closureId);
}
