package com.sparta.elasticsearch.service;

import com.sparta.elasticsearch.dto.SearchAutoTitleDto;
import com.sparta.elasticsearch.dto.response.SearchAutocompleteDto;
import com.sparta.elasticsearch.dto.response.SearchConcertListDto;
import com.sparta.elasticsearch.dto.SearchConcertResponseDto;
import com.sparta.elasticsearch.entity.ConcertSearch;
import com.sparta.elasticsearch.entity.SearchTerm;
import com.sparta.elasticsearch.repository.SearchTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SearchTermRepository searchTermRepository;

    /* 자동완성 */
    public SearchAutocompleteDto autocomplete(String prefix) {
        Sort sort = Sort.by(Sort.Order.desc("count"));
        List<ConcertSearch> concertSearches = searchTermRepository.findByTitleStartingWithOrderByCountDesc(prefix, sort);
        List<SearchAutoTitleDto> titles = concertSearches.stream().map(SearchAutoTitleDto::from).toList();
        return SearchAutocompleteDto.of(titles);
    }

    /* 멀티필드검색 및 오타 허용 검색 (공연이름, 아티스트 이름) */
    public SearchConcertListDto searchConcerts(String query, Pageable pageable) {
        Page<ConcertSearch> concerts = searchTermRepository.search(query, pageable);
        List<SearchConcertResponseDto> concertSimpleDtoList = concerts.map(SearchConcertResponseDto::from).toList();
        return SearchConcertListDto.of(concertSimpleDtoList);
    }

    /* 검색어 카운트 업데이트 및 캐시 초기화 */
    public void updateSearchCount(String title) {
        // 'title'로 기존 ConcertSearch 객체 검색
        ConcertSearch concertSearch = searchTermRepository.findByTitle(title).orElseGet(
                () -> {
                    // 해당 title이 없을 경우 새로운 ConcertSearch 객체 생성 및 초기화
                    ConcertSearch newConcertSearch = new ConcertSearch();
                    newConcertSearch.initializeQueryAndCount(title, 0L);  // title 설정
                    return newConcertSearch;
                });

        // 기존 객체가 존재하면 count 증가
        if (concertSearch.getCount() != null) {
            concertSearch.countUpdate(concertSearch.getCount() + 1);
        }

        // 업데이트된 ConcertSearch 객체 저장
        searchTermRepository.save(concertSearch);
    }
}
