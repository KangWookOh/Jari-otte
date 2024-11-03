package com.sparta.elasticsearch.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SearchConcertResponseDto {
    private Long concertId;
    private String title;
    private List<String> artists;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
