package com.zsj.roombooking.model.criteria;

import com.zsj.roombooking.model.RoomStatus;

public record RoomSearchCriteria(String name,
                                 Integer minCapacity,
                                 Integer maxCapacity,
                                 String area,
                                 RoomStatus status) {
}
