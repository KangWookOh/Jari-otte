package com.sparta.elasticsearch.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SearchConcertResponseDto {
    private Long concertId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
