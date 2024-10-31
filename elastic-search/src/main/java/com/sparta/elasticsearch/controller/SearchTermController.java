package com.sparta.elasticsearch.controller;

import com.sparta.elasticsearch.entity.SearchTerm;
import com.sparta.elasticsearch.service.SearchService;
import lombok.RequiredArgsConstructor;
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
}
