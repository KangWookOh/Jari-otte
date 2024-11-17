package com.eatpizzaquickly.concertservice.util;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.eatpizzaquickly.concertservice.dto.SearchAutoTitleDto;
import com.eatpizzaquickly.concertservice.dto.SearchConcertResponseDto;
import com.eatpizzaquickly.concertservice.entity.ConcertSearch;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResultProcessor {

    public static List<SearchAutoTitleDto> processAutocompleteResults(SearchResponse<ConcertSearch> searchResponse, String query) {
        return searchResponse.hits().hits().stream()
                .flatMap(hit -> {
                    assert hit.source() != null;
                    return SearchAutoTitleDto.from(hit.source()).stream();
                })
                // query로 시작하는 항목을 먼저 필터링
                .sorted((dto1, dto2) -> {
                    boolean dto1StartsWithQuery = dto1.getAuto().startsWith(query);
                    boolean dto2StartsWithQuery = dto2.getAuto().startsWith(query);

                    // query로 시작하는 항목이 먼저 오도록 정렬
                    if (dto1StartsWithQuery && !dto2StartsWithQuery) return -1;
                    if (!dto1StartsWithQuery && dto2StartsWithQuery) return 1;

                    // 시작하지 않는 경우는 score 기준 정렬 유지
                    return 0;
                })
                // 중복 제거
                .distinct()
                // 최대 5개만 선택
                .limit(5)
                .collect(Collectors.toList());
    }

    public static List<SearchConcertResponseDto> processConcertSearchResults(SearchResponse<ConcertSearch> searchResponse) {
        return searchResponse.hits().hits().stream()
                .map(Hit::source)                           // Hit 객체에서 ConcertSearch 객체 추출
                .filter(Objects::nonNull)                   // null 값 필터링 (null 값이 있는 경우 리스트에 포함되지 않음)
                .map(SearchConcertResponseDto::from)        // 각 ConcertSearch 객체를 SearchConcertResponseDto로 변환
                .collect(Collectors.toList());              // 결과를 리스트로 수집
    }
}
