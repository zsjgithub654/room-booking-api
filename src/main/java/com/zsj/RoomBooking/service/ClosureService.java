package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.entity.Closure;
import com.zsj.RoomBooking.model.result.AddClosureResult;

import java.time.LocalDateTime;
import java.util.List;

public interface ClosureService {
    Closure getClosure(Long Id);
    List<Closure> getClosuresOfRoom(Long roomId);
    AddClosureResult addClosure(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteClosure(Long closureId);
}
