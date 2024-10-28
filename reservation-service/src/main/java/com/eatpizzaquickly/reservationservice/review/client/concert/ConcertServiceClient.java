package com.eatpizzaquickly.reservationservice.review.client.concert;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "concert-service")
public interface ConcertServiceClient {

    @GetMapping(value = "/api/v1/concerts/{concertId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ConcertDetailResponse getConcert(@PathVariable Long concertId);
}
