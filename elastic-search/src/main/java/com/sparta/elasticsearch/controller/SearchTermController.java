package com.sparta.elasticsearch.controller;

import com.sparta.elasticsearch.common.advice.ApiResponse;
import com.sparta.elasticsearch.dto.response.SearchAutocompleteDto;
import com.sparta.elasticsearch.dto.response.SearchConcertListDto;
import com.sparta.elasticsearch.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchTermController {
    private final SearchService searchService;

    /* 자동 완성 결과 반환 */
    @GetMapping("/Search/autocomplete")
    public ResponseEntity<ApiResponse<SearchAutocompleteDto>> autocomplete(@RequestParam String query) {
        SearchAutocompleteDto autocompleteDto = searchService.autocomplete(query);
        return ResponseEntity.ok(ApiResponse.success("자동완성 리스트 조회 성공", autocompleteDto));
    }

    /* 멀티필드검색 및 오타 허용 검색 (공연이름, 아티스트 이름) */
    @GetMapping("/Search")
    public ResponseEntity<ApiResponse<SearchConcertListDto>> searchConcerts(
            @RequestParam String query,
            @PageableDefault Pageable pageable
    ) {
        SearchConcertListDto searchConcertListDto = searchService.searchConcerts(query, pageable);
        searchService.updateSearchCount(query); // 검색한 횟수 카운트
        return ResponseEntity.ok(ApiResponse.success("공연 리스트 조회 성공", searchConcertListDto));
    }
}
