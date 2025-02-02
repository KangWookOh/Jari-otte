package com.eatpizzaquickly.concertservice.service;

import com.eatpizzaquickly.concertservice.client.RedisCachePublisher;
import com.eatpizzaquickly.concertservice.dto.ConcertSimpleDto;
import com.eatpizzaquickly.concertservice.dto.SeatDto;
import com.eatpizzaquickly.concertservice.dto.request.ConcertCreateRequest;
import com.eatpizzaquickly.concertservice.dto.request.ConcertUpdateRequest;
import com.eatpizzaquickly.concertservice.dto.request.HostIdRequestDto;
import com.eatpizzaquickly.concertservice.dto.response.ConcertDetailResponse;
import com.eatpizzaquickly.concertservice.dto.response.ConcertHostResponseDto;
import com.eatpizzaquickly.concertservice.dto.response.ConcertListResponse;
import com.eatpizzaquickly.concertservice.entity.Concert;
import com.eatpizzaquickly.concertservice.entity.Seat;
import com.eatpizzaquickly.concertservice.entity.Venue;
import com.eatpizzaquickly.concertservice.enums.Category;
import com.eatpizzaquickly.concertservice.exception.NotFoundException;
import com.eatpizzaquickly.concertservice.repository.ConcertRedisRepository;
import com.eatpizzaquickly.concertservice.repository.ConcertRepository;
import com.eatpizzaquickly.concertservice.repository.SeatRepository;
import com.eatpizzaquickly.concertservice.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final ConcertRedisRepository concertRedisRepository;
    private final SearchService searchService;
    private final RedisCachePublisher redisCachePublisher;

    private static final int TOP_CONCERT_LIMIT = 10;

    @Transactional
    public ConcertDetailResponse saveConcert(ConcertCreateRequest concertCreateRequest, Long hostId) {
        Venue venue = venueRepository.findById(concertCreateRequest.getVenueId()).orElseThrow(NotFoundException::new);

        Concert concert = Concert.builder()
                .title(concertCreateRequest.getTitle())
                .hostId(hostId)
                .description(concertCreateRequest.getDescription())
                .artists(concertCreateRequest.getArtists())
                .startDate(concertCreateRequest.getStartDate())
                .endDate(concertCreateRequest.getEndDate())
                .performDate(concertCreateRequest.getPerformDate())
                .category(Category.of(concertCreateRequest.getCategory()))
                .thumbnailUrl(concertCreateRequest.getThumbnailUrl())
                .price(concertCreateRequest.getPrice())
                .venue(venue)
                .seatCount(venue.getSeatCount())
                .build();

        concertRepository.save(concert);

        // Elasticsearch 인덱스 저장
        try {
            searchService.saveIndex(concert);
        } catch (Exception e) {
            System.err.println("Elasticsearch 인덱싱 실패: " + e.getMessage());
        }

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
        List<SeatDto> seatDtoList = seats.stream().map(SeatDto::from).toList();
        concertRedisRepository.addAvailableSeats(concert.getId(), seatDtoList);

        return ConcertDetailResponse.from(concert, venue, venue.getSeatCount());
    }

    public ConcertListResponse findAllConcerts1(Pageable pageable) {
        List<ConcertSimpleDto> concertSimpleDtoList = concertRepository.findAll(pageable).map(ConcertSimpleDto::from).toList();
        return ConcertListResponse.of(concertSimpleDtoList);
    }

    public ConcertListResponse findAllConcerts() {
        List<ConcertSimpleDto> concertSimpleDtoList = concertRepository.findAll().stream().map(ConcertSimpleDto::from).toList();
        return ConcertListResponse.of(concertSimpleDtoList);
    }

    public ConcertDetailResponse findConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId).orElseThrow(NotFoundException::new);
        increaseViewCount(concertId);
        return ConcertDetailResponse.from(concert);
    }

    public ConcertDetailResponse findConcertWithVenue(Long concertId) {
        Concert concert = concertRepository.findByIdWithVenue(concertId).orElseThrow(NotFoundException::new);
        increaseViewCount(concertId);
        return ConcertDetailResponse.from(concert);
    }

    // 삭제
//    public ConcertListResponse searchConcert(String keyword, Pageable pageable) {
//        Page<Concert> concerts = concertRepository.searchByTitleOrArtists(keyword, pageable);
//        List<ConcertSimpleDto> concertSimpleDtoList = concerts.map(ConcertSimpleDto::from).toList();
//        return ConcertListResponse.of(concertSimpleDtoList);
//    }

    @Transactional
    public void deleteConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId).orElseThrow(NotFoundException::new);
        concertRepository.delete(concert);

        // 인덱스 삭제
        try {
            searchService.delIndex(concert.getId());
        } catch (Exception e) {
            System.err.println("Elasticsearch 인덱싱 실패: " + e.getMessage());
        }
    }

    public Page<ConcertSimpleDto> searchByCategory(String category, Pageable pageable) {
        Category vaildCategory = Category.of(category);
        Page<Concert> concert = concertRepository.findAllByCategory(vaildCategory, pageable);
        return concert.map(ConcertSimpleDto::from);
    }

    public List<ConcertSimpleDto> getTopConcerts() {
        List<Long> topViewedConcertIds = concertRedisRepository.getTopConcertsIds(TOP_CONCERT_LIMIT);
        List<Concert> concerts = concertRepository.findAllById(topViewedConcertIds);
        return concerts.stream().map(ConcertSimpleDto::from).toList();
    }

    private void increaseViewCount(Long concertId) {
        concertRedisRepository.increaseViewCount(concertId);
    }


    @Transactional(readOnly = true)
    public ConcertHostResponseDto findHostIdsByConcertIds(HostIdRequestDto hostIdRequestDto) {
        HashSet<Long> concertIds = hostIdRequestDto.getConcertIds();
        log.info("콘서트 ID : {}", concertIds);
        ConcertHostResponseDto responseDto = new ConcertHostResponseDto(new HashMap<>());
        concertRepository.findByConcertIds(concertIds).forEach(
                concert -> responseDto.getResult().put(concert.getId().toString(), concert.getHostId())
        );
        return responseDto;
    }

    @Transactional
    public ConcertDetailResponse updateConcert(Long concertId, ConcertUpdateRequest concertUpdateRequest) {
        Concert concert = concertRepository.findByIdWithVenue(concertId).orElseThrow(NotFoundException::new);
        concert.updateTitle(concertUpdateRequest.getTitle());
        concert.updateDescription(concertUpdateRequest.getDescription());
        concert.updateThumbnailUrl(concertUpdateRequest.getThumbnailUrl());

        if (isTopConcert(concertId)) {
            redisCachePublisher.publishCacheUpdate(concertId);
        }

        return ConcertDetailResponse.from(concert);
    }

    @Transactional
    public void resetTopConcerts() {
        concertRedisRepository.resetTopConcerts();
    }

    // 인기 공연 여부 확인 메서드
    private boolean isTopConcert(Long concertId) {
        return concertRedisRepository.isTopConcert(concertId);
    }

}
