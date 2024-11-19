package com.eatpizzaquickly.batchservice.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "concert-service")
public interface ConcertClient {

    @GetMapping("/api/v1/concerts/{concertId}/host")
    Long findHostIdByConcertId(@PathVariable(name = "concertId") Long concertId);

    @PostMapping("/api/v1/concerts/top")
    void resetTopConcerts();
}
