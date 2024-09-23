package com.BusBooking.BookingService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.BusBooking.BookingService.client.ScheduleServiceClient;
import com.BusBooking.BookingService.client.UserServiceClient;
import com.BusBooking.BookingService.config.EmailInfo;
import com.BusBooking.BookingService.entity.Booking;
import com.BusBooking.BookingService.exception.SeatNotAvailableException;
import com.BusBooking.BookingService.exception.SeatNotReservedException;
import com.BusBooking.BookingService.repository.BookingRepository;
import com.google.gson.Gson;

import java.sql.Timestamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookingService 
{
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ScheduleServiceClient scheduleServiceClient;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    UserServiceClient userServiceClient;

    Gson gson = new Gson();


    public Mono<Boolean> createBooking(int userId, int seatId)
    {
        /*
         * step 1 : reserve the seat in schedule service
           step 2 : payment confirmation -- not implemented yet 
           step 3 : mark the seat as booked in schedule service
           step 4 : create booking record
           step 5 : getting email details from user service and send notification 
         */
        logger.info("Creating Booking for userId: {} with seatId:{}", userId, seatId);
        return  scheduleServiceClient.reserveSeat(seatId)
                .flatMap(reserved -> {
                if (!reserved) {
                    logger.debug("Failed to reserve seat with ID:{}",seatId);
                    return Mono.error(new SeatNotAvailableException("Failed to reserve seat."));
                }
                logger.info("Seat Reserved for userId: {} with seatId:{}", userId, seatId);
                return scheduleServiceClient.confirmSeat(seatId);
                })
                .flatMap(confirmed -> {
                if (!confirmed) {
                    logger.debug("Failed to confirm seat with ID:{}",seatId);
                    return scheduleServiceClient.releaseSeat(seatId)
                           .then(Mono.error(new SeatNotReservedException("Failed to confirm seat.")));
                }
                logger.info("Seat confirmed for userId: {} with seatId:{}", userId, seatId);
                long millis = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(millis);
                Booking booking = Booking.builder()
                                  .userId(userId)
                                  .seatId(seatId)
                                  .status("CONFIRMED")
                                  .bookingDate(timestamp)
                                  .build();

                return   bookingRepository.save(booking)
                         .doOnNext(savedBooking -> logger.info("Booking created for userId:{} with bookingId:{}", userId, savedBooking.getBookingId()))
                         .then(Mono.just(true));
                })
                .flatMap(notUsed -> {
                    return userServiceClient.getEmailByUserId(userId)
                           .doOnNext(email -> {
                            EmailInfo emailInfo = new EmailInfo();
                            emailInfo.setTo(email);
                            emailInfo.setSubject("Booking Confirmed");
                            emailInfo.setBody("Your booking has been successfully confirmed with seat "+ seatId + ". Happy Journey!");
                            sendNotification(emailInfo);
                           })
                           .then(Mono.just(true));
                    }
                )
                .onErrorResume(ex -> {
                    logger.error("error occured while creating booking for userId: {} and seatId:{}",userId,seatId,ex);
                    return Mono.error(ex);
                });
    }

    public Mono<Void> cancelBooking(int bookingId)
    {
        /*
           step 1: Update booking status
           step 2: Payment service refund issue -- not implemented yet 
           step 3: Release the seat
           step 4: getting email details from user service and send notification
         */
        logger.info("cancelling booking with ID:{}",bookingId);
        return bookingRepository.findById(bookingId)
              .switchIfEmpty(Mono.error(new RuntimeException("Booking not found")))
              .flatMap(booking -> {
                booking.setStatus("CANCELLED");
                return  bookingRepository.save(booking)
                        .then(scheduleServiceClient.releaseSeat(booking.getSeatId()))
                        .flatMap(released -> {
                            if (!released) {
                                logger.error("Failed to release seat with ID:{}",booking.getSeatId());
                                return Mono.error(new RuntimeException("Failed to release seat"));
                            }
                            logger.info("Booking cancelled succesfully with ID:{}",booking.getBookingId());
                            return Mono.empty();  
                        });
              });
    }

    public Flux<Booking> getAllBookings(int userId) { 

        return bookingRepository.findByUserId(userId);
        
    }
    public boolean sendNotification(EmailInfo emailInfo){
        try {
            String json = gson.toJson(emailInfo);
            kafkaTemplate.send("booking-notifications",json);
            logger.info("notification event sent to kafka for emailId: {}",emailInfo.getTo());
            return true;
        }
        catch(Exception e)
        {
            logger.error("Failed to send notification event to kafka for emailId: {}",emailInfo.getTo(), e);
             return false;
        }
    }
}
