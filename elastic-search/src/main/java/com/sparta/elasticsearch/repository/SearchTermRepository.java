package com.sparta.elasticsearch.repository;

import com.sparta.elasticsearch.entity.SearchTerm;
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
}
