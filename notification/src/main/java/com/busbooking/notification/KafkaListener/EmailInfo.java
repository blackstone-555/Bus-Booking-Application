package com.busbooking.notification.KafkaListener;

import lombok.Data;

@Data
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
}

