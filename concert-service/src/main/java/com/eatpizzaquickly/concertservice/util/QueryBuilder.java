package com.eatpizzaquickly.concertservice.util;

import co.elastic.clients.elasticsearch._types.query_dsl.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

    public static BoolQuery createAutocompleteQuery(String query) {
        // MultiMatchQuery 구성: 오타 허용과 접두사 일치
        MultiMatchQuery matchQueryWithFuzziness = MultiMatchQuery.of(m -> m
                .query(query)                                         // 사용자가 입력한 검색어
                .fields("title^2", "artists")          // title에 가중치 2배 부여, artists 필드 포함
                .fuzziness("AUTO")                              // 오타 허용
                .operator(Operator.Or)                                // OR 연산자로 매칭
        );

        // 접두사 일치를 위한 MultiMatchQuery 구성
        MultiMatchQuery matchQueryWithPhrasePrefix = MultiMatchQuery.of(m -> m
                .query(query)                                          // 사용자가 입력한 검색어
                .fields("title^2", "artists")           // title과 artists 필드 포함
                .type(TextQueryType.PhrasePrefix)                     // 접두사 일치
        );

        return BoolQuery.of(b -> b
                .should(Query.of(q -> q.multiMatch(matchQueryWithFuzziness)))      // 오타 허용 쿼리
                .should(Query.of(q -> q.multiMatch(matchQueryWithPhrasePrefix)))   // 접두사 일치 쿼리
        );
    }

    public static BoolQuery createConcertSearchQuery(String query, LocalDate startDate, LocalDate endDate) {
        // 다중 필드 검색, 오타 허용
        // title과 artists 필드에서 검색어가 포함된 문서를 찾고, 오타도 허용합니다.
        MultiMatchQuery matchQueryWithFuzziness = MultiMatchQuery.of(m -> m
                .query(query)                                  // 사용자가 입력한 검색어
                .fields("title^2", "artists")   // 검색할 필드 목록 (title과 artists), title에 가중치 2배 부여
                .fuzziness("AUTO")                      // 오타를 허용하여 유사한 검색어도 매칭
                .operator(Operator.Or)                        // 모든 검색어를 포함할 필요 없이 하나만 포함해도 매칭
        );

        // phrase_prefix
        // 검색어가 입력된 단어의 앞부분만 맞아도 매칭되도록 설정
        // 검색어의 접두사에 맞는 문서도 검색
        MultiMatchQuery matchQueryWithPhrasePrefix = MultiMatchQuery.of(m -> m
                .query(query)                                   // 사용자가 입력한 검색어
                .fields("title^2", "artists")    // 검색할 필드 목록 (title과 artists), title에 가중치 2배 부여
                .type(TextQueryType.PhrasePrefix)               // phrase_prefix 타입으로 설정하여 접두사 일치 허용
        );

        // Wildcard Query 추가: artists 필드에 대해 중간에 포함된 텍스트도 매칭
        WildcardQuery wildcardQuery = WildcardQuery.of(w -> w
                .field("artists")
                .value("*" + query + "*")  // 검색어가 포함된 부분 일치 허용
        );

        // BoolQuery에 추가할 필터 리스트 생성
        List<Query> filters = new ArrayList<>();

        // 날짜 필터 추가 (startDate와 endDate가 모두 존재할 경우에만 필터 적용)
        if (startDate != null && endDate != null) {
            // 시작일과 종료일을 UTC 시간의 ISO-8601 문자열로 변환
            String startDateTime = startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toString();
            String endDateTime = endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toString();

            // DateRangeQuery 생성
            DateRangeQuery dateRangeQuery = new DateRangeQuery.Builder()
                    .field("startDate")        // 필드 지정
                    .gte(startDateTime)              // 시작 시간
                    .lte(endDateTime)                // 종료 시간
                    .build();

            // RangeQuery 생성 및 DateRangeQuery 추가
            RangeQuery rangeQuery = new RangeQuery.Builder()
                    .date(dateRangeQuery)      // DateRangeQuery를 RangeQuery에 추가
                    .build();

            // Query.Builder를 사용하여 RangeQuery 추가
            Query rangeQueryWrapper = new Query.Builder()
                    .range(rangeQuery)
                    .build();

            // 필터 리스트에 추가
            filters.add(rangeQueryWrapper);
        }
        // 삭제 여부 필터 추가 (deleted가 false인 문서만 반환)
        TermQuery deletedFilter = TermQuery.of(t -> t
                .field("deleted")
                .value(false) // deleted가 false인 문서만 포함
        );
        filters.add(new Query.Builder().term(deletedFilter).build());

        return BoolQuery.of(b -> b
                .should(Query.of(q -> q.multiMatch(matchQueryWithFuzziness)))        // 첫 번째 쿼리: 오타 허용
                .should(Query.of(q -> q.multiMatch(matchQueryWithPhrasePrefix)))    // 두 번째 쿼리: 접두사 일치
                .should(Query.of(q -> q.wildcard(wildcardQuery)))                   // Wildcard 쿼리 추가
                .filter(filters)                                                    // 필터 조건 추가 (필터가 있을 경우에만 적용)
        );
    }
}
