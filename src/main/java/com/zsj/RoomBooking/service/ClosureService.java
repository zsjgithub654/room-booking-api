package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.dto.request.TimeRangeRequest;
import com.zsj.RoomBooking.model.dto.response.AddClosureResponse;
import com.zsj.RoomBooking.model.dto.response.ClosureResponse;

import java.util.List;

public interface ClosureService {
    ClosureResponse getClosure(Long Id);
    List<ClosureResponse> getClosuresForRoom(Long roomId);
    AddClosureResponse addClosure(Long roomId, Long userId, TimeRangeRequest closureRequest);
    void deleteClosure(Long closureId);
}
