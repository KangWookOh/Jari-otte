package com.eatpizzaquickly.batchservice.concert.service;

import com.eatpizzaquickly.batchservice.common.client.ConcertClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConcertService {

    private final ConcertClient concertClient;

    public void resetTopConcerts() {
        concertClient.resetTopConcerts();
    }
}
