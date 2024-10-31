package com.sparta.elasticsearch.service;

import com.sparta.elasticsearch.entity.SearchTerm;
import com.sparta.elasticsearch.repository.SearchTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
}
