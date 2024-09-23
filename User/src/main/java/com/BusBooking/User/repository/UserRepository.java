package com.BusBooking.User.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.BusBooking.User.entity.User;

import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Integer>
{
    Mono<User> findByEmailid(String emailid);
}
