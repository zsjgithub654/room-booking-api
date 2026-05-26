package com.zsj.RoomBooking.repository;

import com.zsj.RoomBooking.model.entity.Room;
import com.zsj.RoomBooking.model.entity.RoomStatus;
import org.springframework.data.jpa.domain.Specification;

public class RoomSpecifications {
    public static Specification<Room> nameContains(String name) {
        return (root, query, cb) ->
                cb.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<Room> minCapacity(Integer minCapacity) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity);
    }

    public static Specification<Room> maxCapacity(Integer maxCapacity) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("capacity"), maxCapacity);
    }

    public static Specification<Room> inArea(String area) {
        return (root, query, cb) ->
                cb.equal(root.get("area"), area);
    }

    public static Specification<Room> hasStatus(RoomStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }
}