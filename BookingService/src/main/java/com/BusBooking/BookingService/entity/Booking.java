package com.BusBooking.BookingService.entity;

import java.sql.Date;

import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;


@Data
@Builder
//@NoArgsConstructor
@Table(name = "Bookings")
public class Booking {

    @Id
    int bookingId;

    @NotNull(message = "userId cannot be null")
    int userId;

    @NotNull(message = "seatId cannot be null")
    int seatId;

    Timestamp bookingDate;

    String status;
    
}