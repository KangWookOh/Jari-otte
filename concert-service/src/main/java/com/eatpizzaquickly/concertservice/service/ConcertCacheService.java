package com.eatpizzaquickly.concertservice.service;

import com.eatpizzaquickly.concertservice.dto.ConcertSimpleDto;
import com.eatpizzaquickly.concertservice.dto.request.ConcertUpdateRequest;
import com.eatpizzaquickly.concertservice.dto.response.ConcertDetailResponse;
import com.eatpizzaquickly.concertservice.dto.response.ConcertListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ConcertCacheService {

    private final ConcertService concertService;

    @Cacheable(value = "top_concerts", cacheManager = "localCacheManager")
    public List<ConcertSimpleDto> getTopConcertsCache() {
        return concertService.getTopConcerts();
    }

    @CachePut(value = "top_concerts", cacheManager = "localCacheManager")
    public List<ConcertSimpleDto> putTopConcertsCache() {
        return concertService.getTopConcerts();
    }

    @Cacheable(value = "concert_with_venue", key = "#concertId")
    public ConcertDetailResponse findConcertWithVenueCache(Long concertId) {
        return concertService.findConcertWithVenue(concertId);
    }

    @CachePut(value = "concert_with_venue", key = "#concertId")
    public ConcertDetailResponse putConcertCache(Long concertId, ConcertUpdateRequest concertUpdateRequest) {
        return concertService.updateConcert(concertId, concertUpdateRequest);
    }

    @Cacheable(value = "all_concerts")
    public ConcertListResponse findAllConcertsCache() {
        return concertService.findAllConcerts();
    }

    @CachePut(value = "all_concerts")
    public ConcertListResponse putAllConcertsCache() {
        return concertService.findAllConcerts();
    }
 }
