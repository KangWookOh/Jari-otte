package com.eatpizzaquickly.batchservice.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Set;

@FeignClient(name = "concert-service")
public interface ConcertClient {


    @GetMapping("/api/v1/concerts/hosts")
    ResponseEntity<Map<Long,Long>> findHostIdsByConcertIds(@RequestBody Set<Long> concertIds);
}
