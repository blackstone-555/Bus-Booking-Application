package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Bus;
import com.example.exception.BusNotFoundException;
import com.example.repository.BusRepository;

import reactor.core.publisher.Mono;

@Service
public class BusService 
{
    private static final Logger logger = LoggerFactory.getLogger(BusService.class);

    @Autowired
    private BusRepository busRepository;

    public Mono<Bus> saveBus(Bus bus){
        return busRepository.save(bus)
               .doOnNext(savedBus -> logger.info("bus: {} saved successfully with busId:{}",savedBus.getBusNumber(), savedBus.getBusId()));
    }

    public Mono<Bus> updateBus(int busId,Bus bus){
        return busRepository.findByBusId(busId)
               .flatMap(updatedBus -> {
                updatedBus.setBusNumber(bus.getBusNumber());
                updatedBus.setScheduleId(bus.getScheduleId());
                return busRepository.save(updatedBus);
               })
               .doOnNext(savedBus -> logger.info("busId: {} updated successfully",savedBus.getBusId()))
               .switchIfEmpty(Mono.error(new BusNotFoundException("bus not found")))
               .doOnError(error -> logger.error(error.getMessage()));
               
    }

    public Mono<Bus> getBusDetails(int busId){
        return busRepository.findByBusId(busId)
               .switchIfEmpty(Mono.error(new BusNotFoundException("bus not found")))
               .doOnError(error -> logger.error(error.getMessage()));
    }

    public Mono<Void> removeBus(int busId){
        return busRepository.findByBusId(busId)
              .switchIfEmpty(Mono.error(new BusNotFoundException("bus not found")))
              .flatMap(existingBus -> busRepository.deleteById(busId))
              .doOnNext(savedBus -> logger.info("busId: {} deleted successfully",busId))
              .doOnError(error -> logger.error(error.getMessage()));
    }

}
