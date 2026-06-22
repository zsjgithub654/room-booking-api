package com.zsj.roombooking.model;

import java.time.LocalDateTime;

public interface Occupation {
    OccupationType getOccupationType();
    Long getId();
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}
