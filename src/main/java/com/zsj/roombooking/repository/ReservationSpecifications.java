package com.zsj.roombooking.repository;

import com.zsj.roombooking.model.ReservationStatus;
import com.zsj.roombooking.model.entity.Reservation;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ReservationSpecifications {
    public static Specification<Reservation> hasUserId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Reservation> hasRoomId(Long roomId) {
        return (root, query, cb) ->
                cb.equal(root.get("room").get("id"), roomId);
    }

    public static Specification<Reservation> startsAtOrAfter(LocalDateTime fromTime) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("startTime"), fromTime);
    }

    public static Specification<Reservation> startsBefore(LocalDateTime toTime) {
        return (root, query, cb) ->
                cb.lessThan(root.get("startTime"), toTime);
    }

    public static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }
}
