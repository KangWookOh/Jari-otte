package com.eatpizzaquickly.concertservice.dto.response;

import com.eatpizzaquickly.concertservice.entity.Concert;
import com.eatpizzaquickly.concertservice.entity.Venue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ConcertDetailResponse {
    private Long concertId;
    private String title;
    private String location;
    private String description;
    private String artists;
    private Integer seatCount;
    private int price;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime performDate;

    private String thumbnailUrl;

    public static ConcertDetailResponse from(Concert concert, Venue venue, int seatCount) {
        return new ConcertDetailResponse(
                concert.getId(),
                concert.getTitle(),
                venue.getLocation(),
                concert.getDescription(),
                concert.getArtists().toString(),
                seatCount,
                concert.getPrice(),
                concert.getStartDate(),
                concert.getEndDate(),
                concert.getPerformDate(),
                concert.getThumbnailUrl()
        );
    }

    public static ConcertDetailResponse from(Concert concert) {
        return new ConcertDetailResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getVenue().getLocation(),
                concert.getDescription(),
                concert.getArtists().toString(),
                concert.getVenue().getSeatCount(),
                concert.getPrice(),
                concert.getStartDate(),
                concert.getEndDate(),
                concert.getPerformDate(),
                concert.getThumbnailUrl()
        );
    }
}
