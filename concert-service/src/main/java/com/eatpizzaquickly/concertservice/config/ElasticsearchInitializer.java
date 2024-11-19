package com.eatpizzaquickly.concertservice.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.json.JsonData;
import com.eatpizzaquickly.concertservice.exception.detail.ElasticsearchIndexException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ElasticsearchInitializer {
    private final ElasticsearchClient client;
    private final JacksonConfig jacksonConfig;

    public void initializeIndex(String indexName) throws Exception {
        // 인덱스가 존재하지 않는지 확인
        if (!indexExists(indexName)) {
            try {
                // 1. settings.json 로드
                Map<String, Object> settingsMap;
                try {
                    settingsMap = loadJsonFileAsMap("settings/settings.json");
                } catch (IOException e) {
                    throw new ElasticsearchIndexException("settings.json 파일 로드 중 오류 발생", e);
                } catch (Exception e) {
                    throw new ElasticsearchIndexException("settings.json 처리 중 일반적인 오류 발생", e);
                }

                // 2. mappings.json 로드
                JsonNode mappingsNode;
                try {
                    mappingsNode = readJsonFromFile("settings/mappings.json");
                } catch (IOException e) {
                    throw new ElasticsearchIndexException("mappings.json 파일 로드 중 오류 발생: " + e.getMessage(), e);
                } catch (Exception e) {
                    throw new ElasticsearchIndexException("mappings.json 처리 중 일반적인 오류 발생", e);
                }

                // 3. "mappings" 노드 확인
                JsonNode mappingsData = mappingsNode.path("mappings");
                if (mappingsData.isMissingNode()) {
                    throw new ElasticsearchIndexException("mappings.json에 'mappings' 필드가 포함되어 있지 않습니다.");
                }

                // 4. 추출된 "mappings" 데이터를 JSON 문자열로 변환
                String mappingsJsonString = jacksonConfig.objectMapper().writeValueAsString(mappingsData);
                InputStream mappingsStream = new ByteArrayInputStream(mappingsJsonString.getBytes(StandardCharsets.UTF_8));

                // 5. JsonData를 사용하여 설정 적용
                IndexSettings settings = IndexSettings.of(builder -> {
                    settingsMap.forEach((key, value) -> builder.otherSettings(key, JsonData.of(value)));
                    return builder;
                });

                // 6. 인덱스 생성 요청
                CreateIndexRequest request = CreateIndexRequest.of(builder -> builder
                        .index(indexName) // 인덱스 이름 설정
                        .settings(settings) // settings 적용
                        .mappings(m -> m.withJson(mappingsStream)) // mappings 적용
                );

                // Elasticsearch 클라이언트를 사용하여 인덱스 생성 요청 실행
                client.indices().create(request);
                System.out.println("인덱스가 생성되었습니다. 인덱스 이름: " + indexName);

            } catch (IOException e) {
                throw new ElasticsearchIndexException("Elasticsearch 파일 처리 중 입출력 오류 발생", e);
            } catch (Exception e) {
                throw new ElasticsearchIndexException("Elasticsearch 인덱스 생성 중 예외 발생", e);
            }
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
            return jacksonConfig.objectMapper().readValue(inputStream, new TypeReference<Map<String, Object>>() {});
        }
    }

    // 지정된 경로에서 JSON 파일을 읽어와 JsonNode로 반환하는 메서드
    private JsonNode readJsonFromFile(String path) throws IOException {
        // try-with-resources를 사용하여 InputStream을 생성하고, 파일을 클래스패스에서 읽어옴
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            // 파일을 찾지 못한 경우 예외를 발생시킴
            if (input == null) {
                throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + path);
            }
            // ObjectMapper를 사용하여 InputStream에서 JSON 데이터를 읽어 JsonNode로 변환하여 반환
            return jacksonConfig.objectMapper().readTree(input);
        }
    }
}
