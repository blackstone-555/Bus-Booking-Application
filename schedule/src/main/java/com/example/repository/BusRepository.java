package com.example.repository;

import com.example.controller.*;
import com.example.entity.Bus;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Repository
public interface BusRepository extends ReactiveCrudRepository<Bus, Integer> {
    Flux<Bus> findByScheduleId(int scheduleId);
    Mono<Bus> findByBusId(int busId);
}
