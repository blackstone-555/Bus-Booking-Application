package com.example.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.example.controller.*;
import com.example.entity.Seat;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Repository
public interface SeatRepository extends ReactiveCrudRepository<Seat, Integer> {

   
    Flux<Seat> findByBusId(int busId);
    
    Mono<Seat> findBySeatId(int seatId);

}