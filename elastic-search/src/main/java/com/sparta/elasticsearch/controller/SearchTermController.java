package com.sparta.elasticsearch.controller;

import com.sparta.elasticsearch.common.advice.ApiResponse;
import com.sparta.elasticsearch.dto.response.SearchConcertListDto;
import com.sparta.elasticsearch.dto.response.SearchConcertResponseDto;
import com.sparta.elasticsearch.entity.SearchTerm;
import com.sparta.elasticsearch.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchTermController {
    private final SearchService searchService;

    /* 자동 완성 결과 반환 */
    @GetMapping("/Searchs/autocomples")
    public List<SearchTerm> autocomplete(@RequestParam String query) {
        return searchService.autocomplete(query);
    }

    /* 검색 시 카운트 증가 및 자동 완성 결과 반환 */
    @GetMapping("/Searchs")
    public List<SearchTerm> search(@RequestParam String query) {
        searchService.updateSearchCount(query);
        return searchService.autocomplete(query);
    }

    /* 멀티필드검색 및 오타 허용 검색 (공연이름, 아티스트 이름) */
    @GetMapping("/Search")
    public ResponseEntity<ApiResponse<SearchConcertListDto>> searchConcerts(
            @RequestParam String query,
            @PageableDefault Pageable pageable
    ) {
        SearchConcertListDto searchConcertListDto = searchService.searchConcerts(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("공연 리스트 조회 성공", searchConcertListDto));
    }
}
