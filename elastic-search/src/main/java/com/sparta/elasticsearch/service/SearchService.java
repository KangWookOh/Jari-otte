package com.sparta.elasticsearch.service;

import com.sparta.elasticsearch.common.advice.ApiResponse;
import com.sparta.elasticsearch.dto.response.SearchConcertListDto;
import com.sparta.elasticsearch.dto.response.SearchConcertResponseDto;
import com.sparta.elasticsearch.entity.ConcertSearch;
import com.sparta.elasticsearch.entity.SearchTerm;
import com.sparta.elasticsearch.repository.SearchTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SearchTermRepository searchTermRepository;

    /* 캐싱을 적용하여 인기 검색어를 가져온다. */
    @Cacheable(value = "autocompleteCache", key = "#prefix")
    public List<SearchTerm> autocomplete(String prefix) {
        return searchTermRepository.findTop10ByQueryStartingWithOrderByCountDesc(prefix);
    }

    /* 검색어 카운트 업데이트 및 캐시 초기화 */
    @CacheEvict(value = "autocompleteCache", allEntries = true)
    public void updateSearchCount(String query) {
        SearchTerm searchTerm = searchTermRepository.findByQuery(query).orElseGet(
                () -> {
                    SearchTerm newSearchTerm = new SearchTerm();
                    newSearchTerm.initializeQueryAndCount(query, 1L);
                    return newSearchTerm;
                });

        // 기존 객체의 경우 count를 증가시켜 설정
        if (searchTerm.getCount() != null) {
            searchTerm.countUpdate(searchTerm.getCount() + 1);
        }

        searchTermRepository.save(searchTerm);
    }

    /* 멀티필드검색 및 오타 허용 검색 (공연이름, 아티스트 이름) */
    public SearchConcertListDto searchConcerts(String query, Pageable pageable) {
        Page<ConcertSearch> concerts = searchTermRepository.search(query, pageable);
        List<SearchConcertResponseDto> concertSimpleDtoList = concerts.map(SearchConcertResponseDto::from).toList();
        return SearchConcertListDto.of(concertSimpleDtoList);
    }
}
