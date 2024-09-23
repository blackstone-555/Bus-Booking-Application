package com.BusBooking.BookingService.exception;

public class SeatNotReservedException extends RuntimeException {
    public SeatNotReservedException(String message) {
        super(message);
    }
}