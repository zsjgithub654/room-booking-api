package com.zsj.RoomBooking.service;

import com.zsj.RoomBooking.model.ReservationRequest;
import com.zsj.RoomBooking.model.ReservationResponse;
import com.zsj.RoomBooking.model.SearchReservationRequest;
import com.zsj.RoomBooking.model.TimeRangeRequest;

import java.util.List;

public interface ReservationService {
    List<ReservationResponse> searchReservations(SearchReservationRequest searchReservationRequest);
    ReservationResponse getReservation(Long id);
    ReservationResponse addReservation(ReservationRequest reservationRequest);
    ReservationResponse deleteReservation(Long reservationId);
    ReservationResponse updateReservationTime(Long id, TimeRangeRequest request);
}