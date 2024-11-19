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
        return ResponseEntity.ok(ApiResponse.success("공연 생성 성공", concertDetailResponse));
    }

    @GetMapping("/{concertId}")
    public ResponseEntity<ApiResponse<ConcertDetailResponse>> getConcert(@PathVariable Long concertId) {
        ConcertDetailResponse concertDetailResponse = concertService.findConcert(concertId);
        return ResponseEntity.ok(ApiResponse.success("공연 조회 성공", concertDetailResponse));
    }

    @PostMapping("/hosts")
    ResponseEntity<ConcertHostResponseDto> findHostIdsByConcertIds(@RequestBody HostIdRequestDto hostIdRequestDto){
        return ResponseEntity.ok(concertService.findHostIdsByConcertIds(hostIdRequestDto));
    }

    ;
    // 삭제
//    @GetMapping("/search")
//    public ResponseEntity<ApiResponse<ConcertListResponse>> getConcertSearchList(@RequestParam(required = false) String keyword, @PageableDefault Pageable pageable) {
//        ConcertListResponse concertListResponse = concertService.searchConcert(keyword, pageable);
//        return ResponseEntity.ok(ApiResponse.success("공연 리스트 조회 성공", concertListResponse));
//    }

    @GetMapping
    public ResponseEntity<ApiResponse<ConcertListResponse>> getConcertList(@PageableDefault Pageable pageable) {
        ConcertListResponse concertListResponse = concertService.findAllConcerts(pageable);
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
        concertService.updateConcert(concertId, concertUpdateRequest);
        String concertTitle = concertService.findConcert(concertId).getTitle();
        System.out.println("updateConcert:concertTitle = " + concertTitle);
        return ResponseEntity.ok(ApiResponse.success("공연 업데이트 성공"));
    }

    @PostMapping("/top")
    public void resetTopConcerts() {
        concertService.resetTopConcerts();
    }

}
