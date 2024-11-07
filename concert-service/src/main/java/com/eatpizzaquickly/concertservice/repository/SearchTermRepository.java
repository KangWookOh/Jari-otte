package com.eatpizzaquickly.concertservice.repository;

import com.eatpizzaquickly.concertservice.entity.ConcertSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchTermRepository extends ElasticsearchRepository<ConcertSearch, Long> {
}
