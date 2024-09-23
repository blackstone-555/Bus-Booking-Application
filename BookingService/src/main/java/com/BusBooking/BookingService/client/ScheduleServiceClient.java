package com.BusBooking.BookingService.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;

@Component
public class ScheduleServiceClient {

    private final WebClient webClient;

    @Value("${schedule-service.url}")
    private String scheduleServiceUrl;

    public ScheduleServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // Reserve a seat by calling the Schedule Service.
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public Mono<Boolean> reserveSeat(int seatId) {
        String url = scheduleServiceUrl+ "/" + "seats" + "/" + seatId + "/reserve";
        return  webClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorResume((ex) -> Mono.just(false));
    }

    // Confirm a seat by calling the Schedule Service.
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public Mono<Boolean> confirmSeat(int seatId) {
        String url = scheduleServiceUrl+ "/" + "seats" + "/" + seatId + "/confirm";
        return  webClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorResume((ex) -> Mono.just(false));
    }

    // Release a seat by calling the Schedule Service.
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public Mono<Boolean> releaseSeat(int seatId) {
        String url = scheduleServiceUrl+ "/" + "seats" + "/" + seatId + "/release";
        return  webClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorResume((ex) -> Mono.just(false));
    }
}
