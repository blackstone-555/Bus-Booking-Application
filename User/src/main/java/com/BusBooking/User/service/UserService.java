package com.BusBooking.User.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.BusBooking.User.entity.User;
import com.BusBooking.User.exception.InvalidCredentialException;
import com.BusBooking.User.exception.NotRegisteredException;
import com.BusBooking.User.exception.UserNotFoundException;
import com.BusBooking.User.repository.UserRepository;

import reactor.core.publisher.Mono;


@Service
public class UserService 
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Mono<User> getUser(int id)
    {
        return userRepository.findById(id)
               .switchIfEmpty(Mono.error(new UserNotFoundException("user not found")));
    }

    public Mono<User> saveUser(User user)
    {
        try
        {
           user.setPasshash(passwordEncoder.encode(user.getPasshash()));
           return userRepository.save(user);
        }
        catch(DataIntegrityViolationException e)
        {
            return Mono.error(e);
        }
    }
    public Mono<Object> getUserEmailId(int id)
    {
            return  userRepository.findById(id)
                    .switchIfEmpty(Mono.error(new UserNotFoundException("user not found")))
                    .flatMap(user -> {
                        return Mono.just(user.getEmailid());
                    });
    }
    public Mono<User> updateUser(User user)
    {
        return userRepository.findByEmailid(user.getEmailid())
               .flatMap(updateUser -> {
                updateUser.setName(user.getName());
                updateUser.setPasshash(passwordEncoder.encode(user.getPasshash()));
                updateUser.setEmailid(user.getEmailid());
                updateUser.setPhoneno(user.getPhoneno());
                updateUser.setRole(user.getRole());
                return userRepository.save(updateUser);
               })
               .switchIfEmpty(Mono.error(new UserNotFoundException("user not found")));
    }

    public Mono<Void> deleteUser(int id)
    {
        return userRepository.findById(id)
               .switchIfEmpty(Mono.error(new UserNotFoundException("user not found")))
               .flatMap(exists -> userRepository.deleteById(id));
    }
    
    public Mono<Object> validateCredentials(String emailid,String password)
    {
        return  userRepository.findByEmailid(emailid)
                .switchIfEmpty(Mono.error(new UserNotFoundException("user not found")))
                .flatMap(user -> {
                    if(passwordEncoder.matches(password,user.getPasshash())){
                        return Mono.just("valid Credentials");
                    }
                    else{
                        return Mono.error(new InvalidCredentialException("Invalid Credentials"));
                    }
                });
    }

    public Mono<String> getRole(String emailid) {
    return  userRepository.findByEmailid(emailid)
           .switchIfEmpty(Mono.error(new UserNotFoundException("user not found")))
           .map(user -> user.getRole()) 
           .switchIfEmpty(Mono.just("Role not found"));
    }

    public Mono<User> checkUserRegistered(String emailid)
    {
        return  userRepository.findByEmailid(emailid)
               .switchIfEmpty(Mono.error(new NotRegisteredException("user not registered")));

    }
    
}
