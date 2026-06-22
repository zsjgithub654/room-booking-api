package com.zsj.roombooking.model;

public enum ReservationStatus {
    RESERVATION_STATUS_SCHEDULED,
    /* canceled by user or admin */
    RESERVATION_STATUS_CANCELED,
    /* closed caused by closure, room deleted, or user deleted */
    RESERVATION_STATUS_CLOSED
}