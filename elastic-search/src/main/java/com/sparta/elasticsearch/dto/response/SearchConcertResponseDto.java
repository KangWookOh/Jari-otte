package com.sparta.elasticsearch.dto.response;

import com.sparta.elasticsearch.entity.ConcertSearch;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public class SearchConcertResponseDto {
    private Long concertId;
    private String title;
    private List<String> artists;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static SearchConcertResponseDto from(ConcertSearch concertSearch) {
        return new SearchConcertResponseDto(
                concertSearch.getConcertId(),
                concertSearch.getTitle(),
                concertSearch.getArtists(),
                concertSearch.getStartDate(),
                concertSearch.getEndDate());
    }
}
