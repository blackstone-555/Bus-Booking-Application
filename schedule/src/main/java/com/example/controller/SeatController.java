package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authentication.JwtUtil;
import com.example.entity.Seat;
import com.example.service.*;

import reactor.core.publisher.Mono;
import jakarta.servlet.http.HttpServletRequest;
import com.example.exception.*;

    
    @CrossOrigin(origins = "http://localhost:8083")
    @RestController
    @RequestMapping("/busbooking")
    public class SeatController 
    {
    
    @Autowired
    SeatService seatService;
    
    @Autowired
    JwtUtil jwtUtil;

    @GetMapping("/seats/buses/{busId}")
    public Mono<ResponseEntity<Object>> getAllSeats(@PathVariable int busId,HttpServletRequest request)
    {
        return      jwtUtil.getJwtFromRequest(request)
                    .flatMap(this::validateToken)  // Validate the token
                    .flatMap(notUsed -> seatService.getAllSeatsByBus(busId)  // Fetch seats
                    .collectList()  // Convert Flux<Seat> to Mono<List<Seat>>
                    .flatMap(allSeats -> Mono.just(ResponseEntity.ok((Object) allSeats)))  // Return seats as Object
                    )
                    .onErrorResume(this::handleException);
    }   

    @GetMapping("/seats/{seatId}")
    public Mono<ResponseEntity<Object>> getSeatDetails(@PathVariable int seatId,HttpServletRequest request)
    {
        return      jwtUtil.getJwtFromRequest(request)
                    .flatMap(this::validateToken)  
                    .flatMap(notUsed -> seatService.getSeatDetail(seatId))
                    .flatMap(seatDetails -> Mono.just(ResponseEntity.ok((Object) seatDetails)))
                    .onErrorResume(this::handleException);
    }

    @PostMapping("/seats")
    public Mono<ResponseEntity<Object>> addSeat(@RequestBody Seat seat,HttpServletRequest request)
    {
        if (seat.validate()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Request Body"));
        }
        
        return      jwtUtil.getJwtFromRequest(request)
                    .flatMap(this::validateToken)  
                    .flatMap(this::validateAccess)
                    .flatMap(notUsed -> seatService.saveSeat(seat)
                    .then(Mono.just(ResponseEntity.status(HttpStatus.CREATED).body((Object)"Seat added successfully"))))
                    .onErrorResume(this::handleException);
    }

    @PutMapping("/seats/{seatId}")
    public Mono<ResponseEntity<Object>> updateSeat(@RequestBody Seat seat,@PathVariable int seatId,HttpServletRequest request)
    {
        if (seat.validate()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Request Body"));
        }
        return      jwtUtil.getJwtFromRequest(request)
                    .flatMap(this::validateToken)  
                    .flatMap(this::validateAccess)
                    .flatMap(notUsed -> seatService.updateSeat(seat)
                    .then(Mono.just(ResponseEntity.ok((Object)"seat updated successfully"))))
                    .onErrorResume(this::handleException);
    }
    @DeleteMapping("/seats/{seatId}")
    public Mono<ResponseEntity<Object>> deleteSeat(@PathVariable int seatId, HttpServletRequest request)
    {
        return      jwtUtil.getJwtFromRequest(request)
                    .flatMap(this::validateToken)  
                    .flatMap(this::validateAccess)
                    .flatMap(notUsed -> seatService.removeSeat(seatId)
                    .then(Mono.just(ResponseEntity.ok((Object)"seat deleted successfully"))))
                    .onErrorResume(this::handleException);
    }
    @PostMapping("/seats/{seatId}/reserve")
    public Mono<ResponseEntity<Object>> reserveSeat(@PathVariable int seatId) {
          return   seatService.reserveSeat(seatId)
                   .flatMap(notUsed -> Mono.just(ResponseEntity.ok((Object)"Seat reserved successfully")))
                   .onErrorResume(this::handleException);
    }

    @PostMapping("/seats/{seatId}/confirm")
    public Mono<ResponseEntity<Object>> confirmSeat(@PathVariable int seatId) {
        
            return seatService.confirmSeat(seatId)
                  .flatMap(notUsed -> Mono.just(ResponseEntity.ok((Object)"Seat confirmed successfully")))
                  .onErrorResume(this::handleException);
    }

    @PostMapping("/seats/{seatId}/release")
    public Mono<ResponseEntity<Object>> releaseSeat(@PathVariable int seatId) {
            
            return seatService.releaseSeat(seatId)
                   .flatMap(notUsed -> Mono.just(ResponseEntity.ok((Object)"Seat released successfully")))
                   .onErrorResume(this::handleException);
    }

    private Mono<String> validateToken(String token) {        

            return jwtUtil.extractUsername(token)
                .flatMap(emailIdFromToken -> jwtUtil.validateToken(token, emailIdFromToken)
                .flatMap(isValid -> {
                        if (!isValid) {
                            return Mono.error(new InvalidTokenException("Invalid JWT token"));
                        }
                        return Mono.just(token);
                    })
                );
        }
        
        
    private Mono<String> validateAccess(String token) {
        
        return  jwtUtil.extractRole(token)
                .flatMap(role -> {
                    if (role.equals("user")) {
                        return Mono.error(new AccessDeniedException("Access denied"));
                    }
                    return Mono.just(token);  
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
            else if(throwable instanceof SeatNotFoundException)
            {
                SeatNotFoundException e = (SeatNotFoundException) throwable;
                return Mono.just(ResponseEntity.status(404).body(e.getMessage()));
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
