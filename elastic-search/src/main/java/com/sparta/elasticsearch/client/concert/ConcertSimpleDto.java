package com.sparta.elasticsearch.client.concert;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ConcertSimpleDto {
    private Long concertId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
