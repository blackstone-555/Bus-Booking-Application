package com.BusBooking.BookingService.config;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailInfo 
{
    String to;

    String subject;

    String body;

    public boolean validate()
    {
        if(to==null || to.isEmpty() || subject==null || subject.isEmpty() || body==null || body.isEmpty())
        {
            return false;
        }
        return true;
    }
    public void details()
    {
        System.out.println(this.to+" "+this.subject+" "+this.body);
    }
}
