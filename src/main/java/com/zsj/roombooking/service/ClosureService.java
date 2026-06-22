package com.zsj.roombooking.service;

import com.zsj.roombooking.model.entity.Closure;
import com.zsj.roombooking.model.result.AddClosureResult;

import java.time.LocalDateTime;
import java.util.List;

public interface ClosureService {
    Closure getClosure(Long Id);
    List<Closure> getClosuresOfRoom(Long roomId);
    AddClosureResult addClosure(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteClosure(Long closureId);
}
