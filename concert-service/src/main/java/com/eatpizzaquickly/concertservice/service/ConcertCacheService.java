package com.eatpizzaquickly.concertservice.service;

import com.eatpizzaquickly.concertservice.dto.ConcertSimpleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ConcertCacheService {

    private final ConcertService concertService;

    @Cacheable(value = "topConcerts", cacheManager = "localCacheManager")
    public List<ConcertSimpleDto> getTopConcertsCache() {
        return concertService.getTopConcerts();
    }

    @CachePut(value = "topConcerts", cacheManager = "localCacheManager")
    public List<ConcertSimpleDto> putTopViewedConcertsCache() {
        return concertService.getTopConcerts();
    }

 }
