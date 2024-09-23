package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authentication.JwtUtil;
import com.example.entity.Bus;
import com.example.exception.AccessDeniedException;
import com.example.exception.BusNotFoundException;
import com.example.exception.InvalidTokenException;
import com.example.service.BusService;

import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/busbooking")
public class BusController 
{
    @Autowired
    BusService busService;
    
    @Autowired
    JwtUtil jwtUtil;
    
    @PostMapping("/buses")
    public Mono<ResponseEntity<Object>> addBus(@RequestBody Bus bus, HttpServletRequest request) {
        
        if (!bus.validate()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Request Body"));
        }

            return  jwtUtil.getJwtFromRequest(request)
                    .flatMap(this::validateToken) 
                    .flatMap(this::validateAccess)  
                    .flatMap(notUsed -> busService.saveBus(bus)  
                    .then(Mono.just(new ResponseEntity<Object>("Bus added successfully", HttpStatus.CREATED)))
                    )
                    .onErrorResume(this::handleException);
    }

    @GetMapping("/buses/{busId}")
    public Mono<ResponseEntity<Object>> getBus(@PathVariable int busId, HttpServletRequest request) {
            
        return jwtUtil.getJwtFromRequest(request)  
               .flatMap(this::validateToken)       
               .flatMap(notUsed -> busService.getBusDetails(busId))  
               .flatMap(busDetails -> Mono.just(ResponseEntity.ok((Object)busDetails))) 
               .onErrorResume(this::handleException);
    }

    @PutMapping("/buses/{busId}")
    public Mono<ResponseEntity<Object>> updateBus(@RequestBody Bus bus, @PathVariable int busId, HttpServletRequest request) {
        
        if (!bus.validate()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Request Body"));
        }

        return  jwtUtil.getJwtFromRequest(request)
                .flatMap(this::validateToken) 
                .flatMap(this::validateAccess)
                .flatMap(notUsed -> busService.updateBus(busId,bus) 
                .then(Mono.just(new ResponseEntity<Object>("Bus updated successfully", HttpStatus.CREATED)))
                )
                .onErrorResume(this::handleException);
    }

    @DeleteMapping("/buses/{busId}")
    public Mono<ResponseEntity<Object>> deleteBus(@PathVariable int busId,HttpServletRequest request) {   
        
        return jwtUtil.getJwtFromRequest(request)  
               .flatMap(this::validateToken)
               .flatMap(this::validateAccess)
               .flatMap(notUsed -> busService.removeBus(busId)
               .then(Mono.just(ResponseEntity.noContent().build()))
               )
               .onErrorResume(this::handleException);
    }
        
        // Validate the token and return the token string if valid
        private Mono<String> validateToken(String token) {
        
            // Extract the username from the token and validate it
            return jwtUtil.extractUsername(token)
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
            return jwtUtil.extractRole(token)
                .flatMap(role -> {
                    if (role.equals("user")) {
                        return Mono.error(new AccessDeniedException("Access denied"));
                    }
                    return Mono.just(token);  // Return token if access is allowed
                });
        }
        
        private Mono<ResponseEntity<Object>> handleException(Throwable throwable)
        {
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
            else if(throwable instanceof BusNotFoundException)
            {
                BusNotFoundException e = (BusNotFoundException) throwable;
                Mono.just(ResponseEntity.status(404).body(e.getMessage()));
            }
            return Mono.just(ResponseEntity.status(500).body("Server Error"));
        }
        
}
