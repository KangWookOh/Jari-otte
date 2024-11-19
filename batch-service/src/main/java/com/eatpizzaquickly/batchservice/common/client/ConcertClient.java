package com.eatpizzaquickly.batchservice.common.client;

import com.eatpizzaquickly.batchservice.settlement.dto.request.HostIdRequestDto;
import com.eatpizzaquickly.batchservice.settlement.dto.response.ConcertHostResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "concert-service")
public interface ConcertClient {

    @PostMapping("/api/v1/concerts/hosts")
    ResponseEntity<ConcertHostResponseDto> findHostIdsByConcertIds(@RequestBody HostIdRequestDto hostIdRequestDto);

    @PostMapping("/api/v1/concerts/top")
    void resetTopConcerts();
}
