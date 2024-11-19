package com.eatpizzaquickly.concertservice.entity;

import com.eatpizzaquickly.concertservice.enums.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor // 기본 생성자 추가
@Getter
public class ConcertSearch {
    @Id
    private Long concertId;  // Concert ID를 Elasticsearch Document ID로 사용

    private String title;  // 검색할 수 있도록 텍스트 타입으로 설정

    private List<String> artists; // 아티스트

    private Category category; // 카테고리 Enum 타입 필드

    private String thumbnailUrl; // 사진 URL

    private Boolean deleted; // 삭제 여부 필드

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime performDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime endDate;

    // 생성자 또는 다른 메서드에서 startDate와 endDate를 명시적으로 초기화
    public ConcertSearch(Long concertId, String title, List<String> artists, Category category, String thumbnailUrl, Boolean deleted, LocalDateTime performDate, LocalDateTime startDate, LocalDateTime endDate) {
        this.concertId = concertId;
        this.title = title;
        this.artists = artists;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
        this.deleted = deleted;
        this.performDate = performDate;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static ConcertSearch from(Concert concert) {
        return new ConcertSearch(
                concert.getId(),
                concert.getTitle(),
                concert.getArtists(),
                concert.getCategory(),
                concert.getThumbnailUrl(),
                concert.getDeleted(),
                concert.getPerformDate(),
                concert.getStartDate(),
                concert.getEndDate()
        );
    }
}
