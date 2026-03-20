package com.zsj.RoomBooking.model;

public enum ReservationStatus {
    RESERVATION_STATUS_ACTIVE,
    /* canceled by user */
    RESERVATION_STATUS_CANCELED,
    /* closed by admin or due to closure */
    RESERVATION_STATUS_CLOSED
}