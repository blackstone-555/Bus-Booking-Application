package com.BusBooking.User.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.BusBooking.User.authentication.*;
import com.BusBooking.User.entity.User;
import com.BusBooking.User.exception.InvalidCredentialException;
import com.BusBooking.User.exception.InvalidRequestBodyException;
import com.BusBooking.User.exception.InvalidTokenException;
import com.BusBooking.User.exception.NotRegisteredException;
import com.BusBooking.User.exception.UserNotFoundException;
import com.BusBooking.User.service.*;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/busbooking")
public class UserController 
{

    @Autowired
    UserService userService;

    @Autowired
    JwtUtil jwtUtil;

    @GetMapping("/users-service/health")
    public Mono<String> health()
    {
        return Mono.just("200");
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<String>> registerUser(@RequestBody User user) {
        if (!user.validate()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Request Body"));
        }
        return   userService.saveUser(user)
                .map(savedUser -> ResponseEntity.status(HttpStatus.CREATED).body("Registration Successful"))
                .onErrorResume(DataIntegrityViolationException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("Already Registered Login")))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Registration Failed"));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Object>> loginUser(@RequestParam String emailid, @RequestParam String passhash) {
        
        return userService.checkUserRegistered(emailid)
               .flatMap(notUsed -> userService.validateCredentials(emailid, passhash))
               .flatMap(notUsed -> userService.getRole(emailid))
               .flatMap(role -> jwtUtil.generateToken(emailid, role))
               .map(token -> ResponseEntity.ok((Object)("Bearer " + token)))
               .onErrorResume(this::handleException);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Object>> logoutUser(HttpServletRequest request) {
        
        return jwtUtil.getJwtFromRequest(request)
               .flatMap(this::validateToken)
               .flatMap(notUsed -> Mono.just(ResponseEntity.ok((Object)"Logged Out Successfully")))
               .onErrorResume(this::handleException);
    }

    @PutMapping("/users/{id}")
    public Mono<ResponseEntity<Object>> updateUser(@RequestBody User user, @PathVariable int id, HttpServletRequest request) {
        
        if (!user.validate()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Request Body"));
        }
        
        return jwtUtil.getJwtFromRequest(request)
               .flatMap(this::validateToken)
               .flatMap(notUsed -> userService.updateUser(user))
               .then(Mono.just(ResponseEntity.ok((Object)"Updated Successfully")))
               .onErrorResume(this::handleException);
    }

    @DeleteMapping("/users/{id}")
    public Mono<ResponseEntity<Object>> deleteUser(@PathVariable int id, HttpServletRequest request) {
        
        return jwtUtil.getJwtFromRequest(request)
               .flatMap(this::validateToken)
               .flatMap(notUsed -> userService.deleteUser(id)
               .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body((Object)"Deleted Successfully."))))
               .onErrorResume(this::handleException);
    }

    @GetMapping("/users/{id}")
    public Mono<ResponseEntity<Object>> getUser(@PathVariable int id, HttpServletRequest request) {
        
        return jwtUtil.getJwtFromRequest(request)
               .flatMap(this::validateToken)
               .flatMap(notUsed -> userService.getUser(id))
               .flatMap(user -> Mono.just(ResponseEntity.ok((Object)user)))
               .onErrorResume(this::handleException);
    }
    
    @GetMapping("/users/{id}/info")
    public Mono<ResponseEntity<Object>> getUserEmailId(@PathVariable int id) {
        
        return   userService.getUserEmailId(id)
                .flatMap(userInfo -> Mono.just(ResponseEntity.ok((Object)userInfo)))
                .onErrorResume(this::handleException);
    }

    private Mono<Boolean> validateToken(String token) {
        
        return jwtUtil.extractUsername(token)
              .flatMap(emailIdFromToken -> jwtUtil.validateToken(token, emailIdFromToken));
    }
    private Mono<ResponseEntity<Object>> handleException(Throwable throwable)
    {
            if(throwable instanceof InvalidTokenException)
            {
                InvalidTokenException e = (InvalidTokenException) throwable;
                return Mono.just(ResponseEntity.status(401).body(e.getMessage()));
            }
            else if(throwable instanceof InvalidRequestBodyException)
            {
                InvalidRequestBodyException e = (InvalidRequestBodyException)  throwable;
                return Mono.just(ResponseEntity.status(403).body(e.getMessage()));
            }
            else if(throwable instanceof UserNotFoundException)
            {
                UserNotFoundException e = (UserNotFoundException) throwable;
                return Mono.just(ResponseEntity.status(404).body(e.getMessage()));
            }
            else if(throwable instanceof NotRegisteredException)
            {   
                NotRegisteredException e = (NotRegisteredException) throwable;
                return Mono.just(ResponseEntity.status(403).body(e.getMessage()));
            }
            else if(throwable instanceof InvalidCredentialException)
            {
                InvalidCredentialException e = (InvalidCredentialException) throwable;
                return Mono.just(ResponseEntity.status(401).body(e.getMessage()));    
            }
            return Mono.just(ResponseEntity.status(500).body(throwable.getMessage()));
    }
}