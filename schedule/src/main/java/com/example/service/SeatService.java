package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.example.repository.SeatRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.entity.Seat;
import com.example.exception.*;

@Service
public class SeatService 
{
     private static final Logger logger = LoggerFactory.getLogger(SeatService.class);

    @Autowired
    private SeatRepository seatRepository;


    public Flux<Seat> getAllSeatsByBus(int busId) {
        return seatRepository.findByBusId(busId);
    }

    public Mono<Seat> getSeatDetail(int seatId) {
        return seatRepository.findBySeatId(seatId)
               .switchIfEmpty(Mono.error(new SeatNotFoundException("Seat not found")));
    }

    public Mono<Seat> saveSeat(Seat seat) {
        seat.setStatus("AVAILABLE");
        return seatRepository.save(seat)
              .doOnNext(savedSeat -> logger.info("Seat saved successfully with seatId:{} for busId:{}",savedSeat.getSeatId(), savedSeat.getBusId()));
    }

    public Mono<Seat> updateSeat(Seat seat) {
        return seatRepository.findBySeatId(seat.getSeatId())
            .flatMap(existingSeat -> {
                existingSeat.setSeatNumber(seat.getSeatNumber());
                existingSeat.setStatus(seat.getStatus());
                return seatRepository.save(existingSeat);
            })
            .doOnNext(updatedSeat -> logger.info("seatId: {} updated successfully",updatedSeat.getSeatId()))
            .switchIfEmpty(Mono.error(new SeatNotFoundException("Seat not found"))) // Return empty Mono if seat doesn't exist
            .doOnError(error -> logger.error(error.getMessage()));  
    }

    public Mono<Void> removeSeat(int seatId) {
        return seatRepository.findBySeatId(seatId)
               .switchIfEmpty(Mono.error(new SeatNotFoundException("Seat not found")))
               .flatMap(existingSeat -> seatRepository.deleteById(seatId))
               .doOnError(error -> logger.error(error.getMessage()));
        }

    public Mono<Boolean> reserveSeat(int seatId) {
        return seatRepository.findBySeatId(seatId)
                .switchIfEmpty(Mono.error(new SeatNotFoundException("Seat not found" )))
                .flatMap(seat -> {
                if (!"AVAILABLE".equals(seat.getStatus())) {
                    return Mono.error(new SeatNotAvailableException("seatId: {"+seatId+"} is not available"));
                }
                seat.setStatus("RESERVED");
                return seatRepository.save(seat)
                      .then(Mono.just(true))
                      .onErrorResume(OptimisticLockingFailureException.class, e -> {
                        return Mono.error(new SeatNotAvailableException("seat was modified by another transaction"));
                    });
            });
            //.doOnError(error -> logger.error(error.getMessage()));
                
    }

    public Mono<Boolean> confirmSeat(int seatId) {
        return seatRepository.findBySeatId(seatId)
            .switchIfEmpty(Mono.error(new SeatNotFoundException("Seat not found")))
            .flatMap(seat -> {
                if (!"RESERVED".equals(seat.getStatus())) {
                    return Mono.error(new SeatNotReservedException("Seat is not reserved"));
                }
                seat.setStatus("BOOKED");
                return seatRepository.save(seat)
                      .then(Mono.just(true))
                      .onErrorResume(OptimisticLockingFailureException.class, e -> {
                        return Mono.error(new SeatNotAvailableException("seat was modified by another transaction"));
                    });
            });
            //.doOnError(error -> logger.error(error.getMessage()));
    }

    public Mono<Boolean> releaseSeat(int seatId) {
        return seatRepository.findBySeatId(seatId)
            .switchIfEmpty(Mono.error(new SeatNotFoundException("Seat not found")))
            .flatMap(seat -> {
                if ("AVAILABLE".equals(seat.getStatus())) {
                    return Mono.error(new SeatNotReservedException("Seat is not reserved or booked"));
                }
                seat.setStatus("AVAILABLE");
                return seatRepository.save(seat)
                       .then(Mono.just(true))
                       .onErrorResume(OptimisticLockingFailureException.class, e -> {
                        return Mono.error(new SeatNotAvailableException("seat was modified by another transaction"));
                    });
            });
            //.doOnError(error -> logger.error(error.getMessage()));
    }

}
