package com.BusBooking.BookingService.controller;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class BookingInfo 
{
    int userId;
    int seatId;
    public boolean validate()
    {
        if(userId==0 || seatId==0)
        {
            return false;
        }
        return true;
    }
}
