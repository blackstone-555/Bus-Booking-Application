package com.BusBooking.BookingService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

import com.BusBooking.BookingService.authentication.*;
import com.BusBooking.BookingService.client.ScheduleServiceClient;
import com.BusBooking.BookingService.config.EmailInfo;
import com.BusBooking.BookingService.exception.AccessDeniedException;
import com.BusBooking.BookingService.exception.InvalidTokenException;
import com.BusBooking.BookingService.exception.SeatNotAvailableException;
import com.BusBooking.BookingService.exception.SeatNotReservedException;
import com.BusBooking.BookingService.service.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.CrossOrigin;


@CrossOrigin(origins = "http://localhost:8084")
@RestController
@RequestMapping("/busbooking")
public class BookingController 
{
    @Autowired
    BookingService bookingService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    ScheduleServiceClient scheduleServiceClient;

    @GetMapping("/bookings/health")
    public String health()
    {
        return "200";
    }

    @PostMapping("/bookings")
    public Mono<ResponseEntity<Object>> createBooking(@RequestBody BookingInfo bookingInfo,HttpServletRequest request)
    {
        if (!bookingInfo.validate()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Request Body"));
        }

        return jwtUtil.getJwtFromRequest(request)  // Fetch JWT token
               .flatMap(this::validateToken)
               .flatMap(notUsed -> bookingService.createBooking(bookingInfo.getUserId(),bookingInfo.getSeatId())
               .then(Mono.just(new ResponseEntity<>((Object)"Booking Confirmed", HttpStatus.CREATED))))
               //.flatMap(notused -> scheduleServiceClient.reserveSeat(bookingInfo.getSeatId()))
               //.flatMap(notUsed -> Mono.just(new ResponseEntity<>((Object)"seat reserved", HttpStatus.CREATED)))
               .onErrorResume(this::handleException);
    }
    

    @GetMapping("/users/{userId}/bookings")
    public Mono<ResponseEntity<Object>> getAllBookings(@PathVariable int userId,HttpServletRequest request)
    {
        return jwtUtil.getJwtFromRequest(request)
               .flatMap(this::validateToken)
               .flatMap(validToken -> 
                     bookingService.getAllBookings(userId)
                    .collectList()
                    .map(bookings -> ResponseEntity.ok((Object) bookings)))
               .onErrorResume(this::handleException); 
    }

    @DeleteMapping("/bookings/{bookingId}")
    public Mono<ResponseEntity<Object>> cancelBooking(@PathVariable int bookingId,HttpServletRequest request)
    {
        return jwtUtil.getJwtFromRequest(request)
               .flatMap(this::validateToken)
               .flatMap(notUsed -> bookingService.cancelBooking(bookingId)
               .then(Mono.just(ResponseEntity.ok((Object)"Booking Canceled"))))
               .onErrorResume(this::handleException);
        }

    private Mono<String> validateToken(String token) {
            if (token == null || token.isEmpty()) {
                return Mono.error(new RuntimeException("JWT token is missing or empty"));
            }
        
            // Extract the username from the token and validate it
            return  jwtUtil.extractUsername(token)
                    .flatMap(emailIdFromToken -> jwtUtil.validateToken(token, emailIdFromToken)
                    .flatMap(isValid -> {
                            if (!isValid) {
                                return Mono.error(new InvalidTokenException("Invalid JWT token"));
                            }
                            return Mono.just(token);  // Return the valid token
                        })
                    );
    }
        
    // Validate access role and return the token if access is allowed
    private Mono<String> validateAccess(String token) {
            return  jwtUtil.extractRole(token)
                    .flatMap(role -> {
                        if (role.equals("user")) {
                            return Mono.error(new AccessDeniedException("Access denied"));
                        }
                        return Mono.just(token);  // Return token if access is allowed
                    });
    }
        
    private Mono<ResponseEntity<Object>> handleException(Throwable throwable){
            if(throwable instanceof AccessDeniedException)
            {
                AccessDeniedException e = (AccessDeniedException)  throwable;
                return Mono.just(ResponseEntity.status(403).body(e.getMessage()));
            }
            else if(throwable instanceof InvalidTokenException)
            {
                InvalidTokenException e = (InvalidTokenException) throwable;
                return Mono.just(ResponseEntity.status(401).body(e.getMessage()));
            }
           else if(throwable instanceof SeatNotAvailableException)
            {
                SeatNotAvailableException e = (SeatNotAvailableException) throwable;
                return Mono.just(ResponseEntity.status(409).body(e.getMessage()));
            }
            else if(throwable instanceof SeatNotReservedException)
            {
                SeatNotReservedException e = (SeatNotReservedException) throwable;
                return Mono.just(ResponseEntity.status(409).body(e.getMessage()));
            }
            return Mono.just(ResponseEntity.status(500).body("Server Error"));
    }
}
