package com.example.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.controller.*;
import com.example.entity.Schedule;
import com.example.repository.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private BusRepository busRepository;

    @Autowired
    private SeatRepository seatRepository;

    public Flux<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }
    
    public Mono<Map<String, Object>> getSchedulesBySourceAndDestination(String source, String destination) {
        return scheduleRepository.findBySourceAndDestination(source, destination)
                .flatMapMany(schedule -> busRepository.findByScheduleId(schedule.getScheduleId()))
                .flatMap(bus -> seatRepository.findByBusId(bus.getBusId())
                        .collectList()
                        .map(seats -> Map.entry(String.valueOf(bus.getBusNumber()), seats))
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(busDetail -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("source", source);
                    response.put("destination", destination);
                    response.put("buses", busDetail);
                    return response;
                })
                .switchIfEmpty(Mono.empty());
    }
}