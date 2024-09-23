package com.example.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.example.controller.*;
import com.example.entity.Schedule;

import reactor.core.publisher.Mono;

@Repository
public interface ScheduleRepository extends ReactiveCrudRepository<Schedule, Integer> 
{
    Mono<Schedule> findBySourceAndDestination(String source, String destination);
}