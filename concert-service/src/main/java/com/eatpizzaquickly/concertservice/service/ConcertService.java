package com.eatpizzaquickly.concertservice.service;

import com.eatpizzaquickly.concertservice.dto.ConcertSimpleDto;
import com.eatpizzaquickly.concertservice.dto.request.ConcertCreateRequest;
import com.eatpizzaquickly.concertservice.dto.response.ConcertDetailResponse;
import com.eatpizzaquickly.concertservice.dto.response.ConcertListResponse;
import com.eatpizzaquickly.concertservice.entity.Category;
import com.eatpizzaquickly.concertservice.entity.Concert;
import com.eatpizzaquickly.concertservice.entity.Seat;
import com.eatpizzaquickly.concertservice.entity.Venue;
import com.eatpizzaquickly.concertservice.exception.NotFoundException;
import com.eatpizzaquickly.concertservice.repository.ConcertRepository;
import com.eatpizzaquickly.concertservice.repository.SeatRepository;
import com.eatpizzaquickly.concertservice.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMapCache;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final RedissonClient redissonClient;

    @Transactional
    public ConcertDetailResponse saveConcert(ConcertCreateRequest concertCreateRequest) {
        Venue venue = venueRepository.findById(concertCreateRequest.getVenueId()).orElseThrow(NotFoundException::new);

        Concert concert = Concert.builder()
                .title(concertCreateRequest.getTitle())
                .description(concertCreateRequest.getDescription())
                .artists(concertCreateRequest.getArtists())
                .startDate(concertCreateRequest.getStartDate())
                .endDate(concertCreateRequest.getEndDate())
                .category(Category.of(concertCreateRequest.getCategory()))
                .thumbnailUrl(concertCreateRequest.getThumbnailUrl())
                .venue(venue)
                .seatCount(venue.getSeatCount())
                .build();

        concertRepository.save(concert);

        RSet<String> availableSeats = redissonClient.getSet("concert:" + concert.getId() + ":available_seats");

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= venue.getSeatCount(); i++) {
            Seat seat = Seat.builder()
                    .seatNumber(i)
                    .concert(concert)
                    .isReserved(false)
                    .build();

            seats.add(seat);
        }
        seatRepository.saveAll(seats);

        // Redis에 예약 가능한 좌석 세팅
        for (Seat seat : seats) {
            availableSeats.add(String.valueOf(seat.getId()));
        }

        return ConcertDetailResponse.from(concert, venue);
    }

    public ConcertDetailResponse findConcert(Long concertId) {
        Concert concert = concertRepository.findByIdWithVenue(concertId).orElseThrow(NotFoundException::new);
        return ConcertDetailResponse.from(concert, concert.getVenue());
    }

    public ConcertListResponse searchConcert(String keyword, Pageable pageable) {
        Page<Concert> concerts = concertRepository.searchByTitleOrArtists(keyword, pageable);
        List<ConcertSimpleDto> concertSimpleDtoList = concerts.map(ConcertSimpleDto::from).toList();
        return ConcertListResponse.of(concertSimpleDtoList);
    }

    @Transactional
    public void deleteConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId).orElseThrow(NotFoundException::new);
        concertRepository.delete(concert);
    }

    public Page<ConcertSimpleDto> searchByCategory(String category, Pageable pageable) {
        Category vaildCategory = Category.of(category);
        Page<Concert> concert = concertRepository.findAllByCategory(vaildCategory, pageable);
        return concert.map(ConcertSimpleDto::from);
    }
}
