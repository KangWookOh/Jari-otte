package com.sparta.elasticsearch.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "search_terms")
@Setting(settingPath = "/settings/settings.json")
@Getter
public class SearchTerm {
    @Id
    private String id; // UUID
    private String query; // 입력한 검색어
    private Long count;

    public SearchTerm() {
        this.count = 0L;
    }

    public void initializeQueryAndCount(String query, Long count) {
        this.query = query;
        this.count = count;
    }

    public void countUpdate(Long count) {
        this.count = count;
    }
}
