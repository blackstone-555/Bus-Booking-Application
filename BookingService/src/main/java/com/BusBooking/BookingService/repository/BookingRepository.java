package com.BusBooking.BookingService.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.BusBooking.BookingService.controller.*;
import com.BusBooking.BookingService.entity.Booking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository 
public interface BookingRepository extends ReactiveCrudRepository<Booking, Integer> 
{   
    Flux<Booking> findByUserId(int userId);
    Mono<Booking> findBySeatId(int seatId);
}