package com.eatpizzaquickly.concertservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import com.eatpizzaquickly.concertservice.dto.SearchAutoTitleDto;
import com.eatpizzaquickly.concertservice.dto.SearchConcertResponseDto;
import com.eatpizzaquickly.concertservice.dto.response.SearchAutocompleteDto;
import com.eatpizzaquickly.concertservice.dto.response.SearchConcertListDto;
import com.eatpizzaquickly.concertservice.entity.Concert;
import com.eatpizzaquickly.concertservice.entity.ConcertSearch;
import com.eatpizzaquickly.concertservice.exception.detail.AutocompleteException;
import com.eatpizzaquickly.concertservice.exception.detail.ConcertSearchException;
import com.eatpizzaquickly.concertservice.exception.detail.ElasticsearchIndexException;
import com.eatpizzaquickly.concertservice.util.QueryBuilder;
import com.eatpizzaquickly.concertservice.util.ResultProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final ElasticsearchClient elasticsearchClient;

    /* 자동 완성 기능 구현 (오타 허용 및 부분 일치) */
    public SearchAutocompleteDto autocomplete(String query) {
        try {
            // BoolQuery 구성
            BoolQuery boolQuery = QueryBuilder.createAutocompleteQuery(query);

            // SearchRequest 생성
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("concerts")                        // 인덱스 이름
                    .query(Query.of(q -> q.bool(boolQuery)))        // BoolQuery 사용
                    .size(10)                                 // 최대 10개 결과만 반환
            );

            // 검색 실행
            SearchResponse<ConcertSearch> searchResponse = elasticsearchClient.search(searchRequest, ConcertSearch.class);

            // 검색 결과를 DTO로 변환 (title과 artists의 각 항목을 개별적으로 추가하고 일치율 기준으로 정렬)
            List<SearchAutoTitleDto> autoList = ResultProcessor.processAutocompleteResults(searchResponse, query);

            // 결과 반환
            return new SearchAutocompleteDto(autoList);

        } catch (IOException e) {
            throw new AutocompleteException("자동완성 검색 실패");
        }
    }

    /* 멀티필드검색 및 오타 허용 검색 (공연이름, 아티스트 이름) */
    public SearchConcertListDto searchConcerts(String query, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            // Bool 쿼리 구성
            // BoolQuery의 should 조건을 사용하여 두 쿼리 중 하나라도 매칭되면 결과에 포함시킵니다.
            BoolQuery boolQuery = QueryBuilder.createConcertSearchQuery(query, startDate, endDate);

            // SearchRequest 생성
            // 검색할 인덱스와 쿼리를 설정하고, 페이지네이션 정보(from과 size)를 설정합니다.
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("concerts")                      // 검색할 인덱스 이름
                    .query(Query.of(q -> q.bool(boolQuery)))      // 위에서 구성한 BoolQuery를 사용
                    .from((int) pageable.getOffset())             // 검색 시작 위치 설정 (페이지네이션의 offset)
                    .minScore(0.3)                          // 최소 스코어 설정
                    .size(pageable.getPageSize())                 // 한 페이지에 포함될 문서 수 설정
            );

            // 검색 실행
            // elasticsearchClient의 search 메서드를 통해 Elasticsearch에 쿼리를 실행, 검색 결과를 가져온다.
            SearchResponse<ConcertSearch> searchResponse = elasticsearchClient.search(searchRequest, ConcertSearch.class);

            // 결과를 SearchConcertResponseDto 로 변환
            List<SearchConcertResponseDto> concertSimpleDtoList = ResultProcessor.processConcertSearchResults(searchResponse);

            // SearchConcertListDto 로 반환
            return SearchConcertListDto.of(concertSimpleDtoList);

        } catch (IOException e) {
            throw new ConcertSearchException("검색 쿼리 실패");
        }
    }

    /* 콘서트 생성 될때 인덱스에 저장 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveIndex(Concert concert) {
        try {
            // Concert 객체를 ConcertSearch로 변환
            ConcertSearch concertSearch = ConcertSearch.from(concert);

            // IndexRequest 생성
            IndexRequest<ConcertSearch> request = IndexRequest.of(i -> i
                    .index("concerts")                         // 인덱스 이름 설정
                    .id(concertSearch.getConcertId().toString())     // 문서 ID 설정
                    .document(concertSearch)                         // 문서 데이터 설정
            );

            // Elasticsearch에 데이터 인덱싱 요청
            IndexResponse response = elasticsearchClient.index(request);

            System.out.println("문서가 인덱싱되었습니다. 문서 ID: " + response.id());

        } catch (Exception e) {
            throw new ElasticsearchIndexException("콘서트 인덱싱 실패");
        }
    }

    /* 콘서트 삭제 시 인덱스에서 해당 문서 삭제 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delIndex(Long concertId) {
        try {
            // DeleteRequest 생성
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index("concerts")          // 인덱스 이름 지정
                    .id(concertId.toString())   // 삭제할 문서의 ID
            );

            // Elasticsearch에 삭제 요청
            DeleteResponse response = elasticsearchClient.delete(request);

            // 삭제 확인 로그
            if (response.result().name().equals("Deleted")) {
                System.out.println("문서가 성공적으로 삭제되었습니다. 문서 ID: " + concertId);
            } else {
                System.out.println("문서가 존재하지 않거나 이미 삭제되었습니다. 문서 ID: " + concertId);
            }

        } catch (Exception e) {
            throw new ElasticsearchIndexException("콘서트 인덱스 삭제 실패");
        }
    }
}
