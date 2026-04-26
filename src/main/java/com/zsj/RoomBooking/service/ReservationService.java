package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.dto.request.ReservationRequest;
import com.zsj.RoomBooking.model.dto.response.ReservationResponse;
import com.zsj.RoomBooking.model.dto.request.SearchReservationRequest;
import com.zsj.RoomBooking.model.dto.request.TimeRangeRequest;

import java.util.List;

public interface ReservationService {
    List<ReservationResponse> searchReservations(SearchReservationRequest searchReservationRequest);
    ReservationResponse getReservation(Long id);
    ReservationResponse addReservation(Long userId, ReservationRequest reservationRequest);
    ReservationResponse deleteReservation(Long reservationId);
    ReservationResponse updateReservationTime(Long id, TimeRangeRequest request);
}