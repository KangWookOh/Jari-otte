package com.sparta.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "concerts")
@NoArgsConstructor // 기본 생성자 추가
@Getter
@Setting
public class ConcertSearch {
    @Id
    @Field(type = FieldType.Long)
    private Long concertId;  // Concert ID를 Elasticsearch Document ID로 사용

    @Field(type = FieldType.Text)
    private String title;  // 검색할 수 있도록 텍스트 타입으로 설정

    @Field(type = FieldType.Text)
    private List<String> artists; // 아티스트

    @Field(type = FieldType.Date, format = { DateFormat.strict_date_optional_time, DateFormat.epoch_millis })
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startDate;

    @Field(type = FieldType.Date, format = { DateFormat.strict_date_optional_time, DateFormat.epoch_millis })
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime endDate;

    // 생성자 또는 다른 메서드에서 startDate와 endDate를 명시적으로 초기화
    public ConcertSearch(Long concertId, String title, List<String> artists, LocalDateTime startDate, LocalDateTime endDate) {
        this.concertId = concertId;
        this.title = title;
        this.artists = artists;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
