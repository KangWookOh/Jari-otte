package com.sparta.elasticsearch.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "concerts")
@Getter
public class ConcertSearch {
    @Id
    private Long concertId;  // Concert ID를 Elasticsearch Document ID로 사용

    @Field(type = FieldType.Text)
    private String title;  // 검색할 수 있도록 텍스트 타입으로 설정

    @Field(type = FieldType.Text)
    private List<String> artists; // 아티스트

    @Field(type = FieldType.Date)
    private LocalDateTime startDate;  // 날짜 기준 필터링을 위해 날짜 타입으로 설정

    @Field(type = FieldType.Date)
    private LocalDateTime endDate;  // 종료 날짜로 검색이나 필터링 가능

    @Field(type = FieldType.Long)
    private Long count;

    public ConcertSearch() {
        this.count = 0L;
    }

    public void initializeQueryAndCount(String title, Long count) {
        this.title = title;
        this.count = count;
    }

    public void countUpdate(Long count) {
        this.count = count;
    }

}
