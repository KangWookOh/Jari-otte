package com.sparta.elasticsearch.dto.response;

import com.sparta.elasticsearch.dto.SearchConcertResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SearchConcertListDto {
    private List<SearchConcertResponseDto> concertList;

    public static SearchConcertListDto of(List<SearchConcertResponseDto> concertSimpleDtoList) {
        return new SearchConcertListDto(concertSimpleDtoList);
    }
}
