package com.sparta.elasticsearch.repository;

import com.sparta.elasticsearch.entity.ConcertSearch;
import com.sparta.elasticsearch.entity.SearchTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchTermRepository extends ElasticsearchRepository<ConcertSearch, String> {

    @Query("""
        {
          "bool": {
            "should": [
              { "term": { "title.keyword": "?0" }},   // 완전일치 조건 (최우선)
              { "prefix": { "title": "?0" }}          // 접두사 일치
            ]
          }
        }
        """)
    List<ConcertSearch> findByTitleStartingWithOrderByCountDesc(String prefix, Sort sort);

    Optional<ConcertSearch> findByTitle(String title);

    /* 멀티 필드 검색, 오타 허용 (fuzziness: AUTO) */
    @Query("{" +
            "\"bool\": {" +
            "\"should\": [" +
            "{\"multi_match\": {" +
            "\"query\": \"?0\"," +
            "\"fields\": [\"title^2\", \"artists\"]," +
            "\"fuzziness\": \"AUTO\"," +
            "\"operator\": \"and\"" +
            "}}," +
            "{\"multi_match\": {" +
            "\"query\": \"?0\"," +
            "\"fields\": [\"title^2\", \"artists\"]," +
            "\"type\": \"phrase_prefix\"" +
            "}}" +
            "]" +
            "}" +
            "}")
    Page<ConcertSearch> search(String query, Pageable pageable);
}
