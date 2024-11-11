package com.eatpizzaquickly.concertservice.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchIndexConfig {
    private final ElasticsearchClient client;
    private final ObjectMapper objectMapper;

    // 사용할 인덱스 이름
    private final String indexName = "concerts";

    @PostConstruct
    public void applySettingsOnly() throws Exception {
        // 인덱스가 존재하지 않는지 확인
        if (!indexExists(indexName)) {
            // JSON 파일에서 설정을 로드하여 맵으로 변환
            Map<String, Object> settingsMap = loadJsonFileAsMap("settings/settings.json");

            // JsonData를 사용하여 otherSettings에 JSON 설정 적용
            IndexSettings settings = IndexSettings.of(builder -> {
                // settingsMap의 각 키-값 쌍을 설정에 추가
                settingsMap.forEach((key, value) -> builder.otherSettings(key, JsonData.of(value)));
                return builder;  // 빌더 반환 추가
            });

            // CreateIndexRequest 객체 생성, 인덱스 이름과 설정 포함
            CreateIndexRequest request = CreateIndexRequest.of(builder -> builder
                    .index(indexName) // 인덱스 이름 설정
                    .settings(settings) // 설정 적용
            );

            // Elasticsearch 클라이언트를 사용하여 인덱스 생성 요청 실행
            client.indices().create(request);
            System.out.println("인덱스가 생성되었습니다. 인덱스 이름: " + indexName);
        } else {
            System.out.println("인덱스가 이미 존재합니다. 인덱스 이름: " + indexName);
        }
    }

    // 인덱스 존재 여부를 확인하는 메서드
    private boolean indexExists(String indexName) throws Exception {
        // ExistsRequest 객체 생성, 존재 여부 확인할 인덱스 이름 설정
        ExistsRequest existsRequest = new ExistsRequest.Builder().index(indexName).build();
        // 존재 여부 확인 후 결과 반환
        return client.indices().exists(existsRequest).value();
    }

    // JSON 파일을 읽어와 Map<String, Object> 형식으로 변환하는 메서드
    private Map<String, Object> loadJsonFileAsMap(String path) throws Exception {
        // try-with-resources로 InputStream 생성, 경로에서 파일 읽기
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            // ObjectMapper를 사용하여 JSON 파일을 맵으로 변환하여 반환
            return objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
        }
    }
}