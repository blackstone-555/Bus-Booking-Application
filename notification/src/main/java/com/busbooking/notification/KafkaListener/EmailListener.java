package com.busbooking.notification.KafkaListener;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.busbooking.notification.service.EmailService;
import com.google.gson.Gson;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class EmailListener {
   
   @Autowired
   EmailService emailService;
   
   Gson gson = new Gson();

   private static final Logger logger = LoggerFactory.getLogger(EmailListener.class);
    
   @KafkaListener(topics = "booking-notifications", groupId = "booking-notification-group")
   public void notification(String json)
   {

    EmailInfo emailInfo = gson.fromJson(json,EmailInfo.class);
    logger.info(emailInfo.getTo()+","+emailInfo.getBody()+","+emailInfo.getSubject());

    if(!emailInfo.validate()) {
        emailService.sendEmail(emailInfo.getTo(), emailInfo.getSubject(), emailInfo.getBody());
    } 
   }

}
