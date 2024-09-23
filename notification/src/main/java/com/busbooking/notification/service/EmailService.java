package com.busbooking.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;



@Service
public class EmailService 
{
    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    
    public void sendEmail(String to, String subject, String body) 
    {
        try
        {
            logger.info("sending email notification to emailId: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("Email Sent Successfully for emailId: {}", to);
        }
        catch(Exception e)
        {
            logger.error("An error occurred: {}", e.getMessage());
        }
        
    }
}
