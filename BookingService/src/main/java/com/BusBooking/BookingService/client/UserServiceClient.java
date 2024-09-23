package com.BusBooking.BookingService.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    @Value("${user-service.url}")
    private String userServiceUrl;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> getEmailByUserId(int userId) {
        String url = userServiceUrl + "/users/" + userId + "/info";
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class);
    }
}
