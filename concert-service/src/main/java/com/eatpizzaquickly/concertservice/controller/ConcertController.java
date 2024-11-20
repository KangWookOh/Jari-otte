package com.eatpizzaquickly.concertservice.controller;

import com.eatpizzaquickly.concertservice.dto.ConcertSimpleDto;
import com.eatpizzaquickly.concertservice.dto.request.ConcertCreateRequest;
import com.eatpizzaquickly.concertservice.dto.request.ConcertUpdateRequest;
import com.eatpizzaquickly.concertservice.dto.request.HostIdRequestDto;
import com.eatpizzaquickly.concertservice.dto.response.*;
import com.eatpizzaquickly.concertservice.service.ConcertCacheService;
import com.eatpizzaquickly.concertservice.service.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/concerts")
@RestController
public class ConcertController {

    private final ConcertService concertService;
    private final ConcertCacheService concertCacheService;

    // 관리자 권한 필요
    @PostMapping
    public ResponseEntity<ApiResponse<ConcertDetailResponse>> createConcert(
            @RequestBody ConcertCreateRequest concertCreateRequest,
            @RequestParam(name = "host") Long hostId
    ) {
        ConcertDetailResponse concertDetailResponse = concertService.saveConcert(concertCreateRequest, hostId);
        concertCacheService.putAllConcertsCache();
        return ResponseEntity.ok(ApiResponse.success("공연 생성 성공", concertDetailResponse));
    }

    @GetMapping("/{concertId}")
    public ResponseEntity<ApiResponse<ConcertDetailResponse>> getConcert(@PathVariable Long concertId) {
        ConcertDetailResponse concertDetailResponse = concertCacheService.findConcertWithVenueCache(concertId);
        return ResponseEntity.ok(ApiResponse.success("공연 조회 성공", concertDetailResponse));
    }

    @PostMapping("/hosts")
    ResponseEntity<ConcertHostResponseDto> findHostIdsByConcertIds(@RequestBody HostIdRequestDto hostIdRequestDto){
        return ResponseEntity.ok(concertService.findHostIdsByConcertIds(hostIdRequestDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ConcertListResponse>> getConcertList() {
//        ConcertListResponse concertListResponse = concertService.findAllConcerts();
        ConcertListResponse concertListResponse = concertCacheService.findAllConcertsCache();
        return ResponseEntity.ok(ApiResponse.success("공연 조회 성공", concertListResponse));
    }

    // 관리자 권한 필요
    @PatchMapping("/{concertId}")
    public ResponseEntity<ApiResponse<Void>> deleteConcert(@PathVariable Long concertId) {
        concertService.deleteConcert(concertId);
        return ResponseEntity.ok(ApiResponse.success("공연 삭제 성공"));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<ConcertSimpleDto>>> searchByCategory(
            @PathVariable(name = "category") String category,
            @PageableDefault Pageable pageable
    ) {
        Page<ConcertSimpleDto> concertSimpleDtos = concertService.searchByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success("조회 성공", concertSimpleDtos));
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<PopularConcertResponse>> getTopConcerts() {
        List<ConcertSimpleDto> topConcerts = concertCacheService.getTopConcertsCache();
        return ResponseEntity.ok(ApiResponse.success("인기 콘서트 조회 성공", PopularConcertResponse.of(topConcerts)));
    }

    @PutMapping("/{concertId}")
    public ResponseEntity<ApiResponse<Void>> updateConcert(@PathVariable Long concertId,
                                                           @RequestBody ConcertUpdateRequest concertUpdateRequest) {
        concertCacheService.putConcertCache(concertId, concertUpdateRequest);
        concertCacheService.putAllConcertsCache();
        return ResponseEntity.ok(ApiResponse.success("공연 업데이트 성공"));
    }

    @DeleteMapping("/top")
    public void resetTopConcerts() {
        concertService.resetTopConcerts();
    }

}
