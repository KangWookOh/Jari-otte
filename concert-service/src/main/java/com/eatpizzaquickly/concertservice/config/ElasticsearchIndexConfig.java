package com.eatpizzaquickly.concertservice.config;

import com.eatpizzaquickly.concertservice.exception.detail.ElasticsearchIndexException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchIndexConfig {
    private final ElasticsearchInitializer elasticsearchInitializer;

    @PostConstruct
    public void applySettingsOnly() {
        try {
            // 사용할 인덱스 이름
            String indexName = "concerts";
            elasticsearchInitializer.initializeIndex(indexName);
        } catch (Exception e) {
            throw new ElasticsearchIndexException("Elasticsearch 인덱스 초기화 중 오류 발생");
        }
    }
}