package com.BusBooking.User;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;


@SpringBootApplication
@EnableR2dbcRepositories(basePackages = "com.BusBooking.User.repository")
public class UserApplication {

	public static void main(String[] args) 
	{
		SpringApplication.run(UserApplication.class, args);
	}

}
