package com.sparta.elasticsearch.repository;

import com.sparta.elasticsearch.entity.ConcertSearch;
import com.sparta.elasticsearch.entity.SearchTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchTermRepository extends ElasticsearchRepository<SearchTerm, String> {

    @Query("{\"prefix\": {\"query\": \"?0\"}}")
    List<SearchTerm> findTop10ByQueryStartingWithOrderByCountDesc(String prefix);
    Optional<SearchTerm> findByQuery(String query);

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
