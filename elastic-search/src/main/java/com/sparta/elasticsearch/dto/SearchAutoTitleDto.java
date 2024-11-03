package com.sparta.elasticsearch.dto;

import com.sparta.elasticsearch.entity.ConcertSearch;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SearchAutoTitleDto {
    private String title;

    public static SearchAutoTitleDto from(ConcertSearch concertSearch) {
        return new SearchAutoTitleDto(
                concertSearch.getTitle());
    }
}
