package com.eatpizzaquickly.concertservice.util;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.eatpizzaquickly.concertservice.dto.SearchAutoTitleDto;
import com.eatpizzaquickly.concertservice.dto.SearchConcertResponseDto;
import com.eatpizzaquickly.concertservice.entity.ConcertSearch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
@Component
public class ResultProcessor {

    public static List<SearchAutoTitleDto> processAutocompleteResults(SearchResponse<ConcertSearch> searchResponse, String query) {
        return searchResponse.hits().hits().stream()
                .flatMap(hit -> {
                    assert hit.source() != null;
                    return SearchAutoTitleDto.from(hit.source(), Optional.ofNullable(hit.score()).orElse(0.0)).stream();
                })
                // query로 시작하는 항목을 먼저 필터링
                .sorted((dto1, dto2) -> {
                    // query로 시작하는 항목이 우선
                    boolean dto1StartsWithQuery = dto1.getAuto().startsWith(query);
                    boolean dto2StartsWithQuery = dto2.getAuto().startsWith(query);

                    // query로 시작하는 항목이 먼저 오도록 정렬
                    if (dto1StartsWithQuery && !dto2StartsWithQuery) return -1;
                    if (!dto1StartsWithQuery && dto2StartsWithQuery) return 1;

                    // 시작 여부가 같을 경우 점수로 정렬
                    return Double.compare(dto2.getScore(), dto1.getScore());
                })
                .distinct()  // 중복 제거
                .limit(5)    // 최대 5개 제한
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
