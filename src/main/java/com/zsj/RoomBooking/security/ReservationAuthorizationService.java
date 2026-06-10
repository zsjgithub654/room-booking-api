package com.zsj.RoomBooking.security;

import com.zsj.RoomBooking.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * To check if the reservation is owned by the user in @PreAuthorize.
 */
@Component
public class ReservationAuthorizationService {
    @Autowired
    private ReservationRepository reservationRepository;

    public boolean isOwner(Long reservationId, Long userId) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> reservation.getUser().getId().equals(userId))
                .orElse(false);
    }
}
