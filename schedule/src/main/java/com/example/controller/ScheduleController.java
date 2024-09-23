package com.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import com.example.authentication.JwtUtil;
import com.example.exception.AccessDeniedException;
import com.example.exception.InvalidTokenException;
import com.example.service.ScheduleService;

@CrossOrigin(origins = "http://localhost:8083")
@RestController
@RequestMapping("/busbooking")
public class ScheduleController 
{

        @Autowired
        ScheduleService scheduleService;

        @Autowired
        JwtUtil jwtUtil;

        @GetMapping("/schedules/health")
        public String health()
        {
            return "200";
        }

        @GetMapping("/schedules")
        public Mono<ResponseEntity<Object>> getSchedules(@RequestParam String source, @RequestParam String destination, HttpServletRequest request) {
         
         return  jwtUtil.getJwtFromRequest(request)
                .flatMap(this::validateToken)
                .flatMap(notUsed -> scheduleService.getSchedulesBySourceAndDestination(source, destination))
                .flatMap(schedules ->  Mono.just(ResponseEntity.ok((Object)schedules)))
                .onErrorResume(this::handleException); // Handle unexpected errors

        }
        
        private Mono<Object> validateToken(String token) {
            
            return  jwtUtil.extractUsername(token)
                    .flatMap(emailIdFromToken -> jwtUtil.validateToken(token, emailIdFromToken))
                    .flatMap(isValid -> {
                        if (!isValid) {
                            return Mono.error(new InvalidTokenException("Invalid JWT token"));
                        }
                        return Mono.just((Object)token);  // Return the valid token
                    });
        }

        private Mono<ResponseEntity<Object>> handleException(Throwable throwable) {

            if(throwable instanceof InvalidTokenException){
                InvalidTokenException e = (InvalidTokenException) throwable;
                return Mono.just(ResponseEntity.status(401).body(e.getMessage()));
            }
            else if(throwable instanceof AccessDeniedException){
                AccessDeniedException e = (AccessDeniedException)  throwable;
                return Mono.just(ResponseEntity.status(403).body(e.getMessage()));
            }

            return Mono.just(ResponseEntity.status(500).body(throwable.getMessage()));

        }

}