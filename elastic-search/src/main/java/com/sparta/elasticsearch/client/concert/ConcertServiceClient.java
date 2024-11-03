package com.sparta.elasticsearch.client.concert;


import com.sparta.elasticsearch.common.advice.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "concert-service")
public interface ConcertServiceClient {

    @GetMapping(value = "/api/v1/concerts/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ConcertListResponse> getConcert(@RequestParam(required = false) String keyword, @PageableDefault Pageable pageable);

}
