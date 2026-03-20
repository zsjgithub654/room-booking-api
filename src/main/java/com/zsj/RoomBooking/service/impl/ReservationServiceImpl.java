package com.zsj.RoomBooking.service.impl;

import com.zsj.RoomBooking.entity.Reservation;
import com.zsj.RoomBooking.model.ReservationRequest;
import com.zsj.RoomBooking.model.ReservationResponse;
import com.zsj.RoomBooking.model.SearchReservationRequest;
import com.zsj.RoomBooking.model.TimeRangeRequest;
import com.zsj.RoomBooking.repository.ReservationRepository;
import com.zsj.RoomBooking.repository.RoomRepository;
import com.zsj.RoomBooking.repository.UserRepository;
import com.zsj.RoomBooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<ReservationResponse> searchReservations(SearchReservationRequest searchReservationRequest) {
        return null;
    }

    @Override
    public ReservationResponse getReservation(Long id) {
        return getReservationResponse(reservationRepository.getReferenceById(id));
    }

    @Override
    public ReservationResponse addReservation(Long userId, ReservationRequest reservationRequest) {
        return null;
    }

    @Override
    public ReservationResponse deleteReservation(Long reservationId) {
        return null;
    }

    @Override
    public ReservationResponse updateReservationTime(Long id, TimeRangeRequest request) {
        return null;
    }

    /**
     * generate a dto object from a Reservation object to return
     * @param reservation: Reservation object
     * @return ReservationResponse object
     */
    private ReservationResponse getReservationResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getRoom().getId(),
                reservation.getStartTime(), reservation.getEndTime(),
                reservation.getReservationStatus());
    }
}
