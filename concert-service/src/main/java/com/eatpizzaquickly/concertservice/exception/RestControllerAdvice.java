package com.eatpizzaquickly.concertservice.exception;

import com.eatpizzaquickly.concertservice.dto.response.ErrorResponse;
import com.eatpizzaquickly.concertservice.exception.detail.AutocompleteException;
import com.eatpizzaquickly.concertservice.exception.detail.ElasticsearchIndexException;
import com.eatpizzaquickly.concertservice.exception.detail.RequestLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@org.springframework.web.bind.annotation.RestControllerAdvice
public class RestControllerAdvice {

    // 상태코드 400, 인덱싱 예외처리
    @ExceptionHandler(ElasticsearchIndexException.class)
    public ResponseEntity<ErrorResponse> handleRequestLimitExceededException(ElasticsearchIndexException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }

    // 상태코드 429, 대기열 예외처리
    @ExceptionHandler(RequestLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRequestLimitExceededException(RequestLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorResponse(e.getMessage()));
    }

    // 상태코드 500, 자동완성, 콘서트 검색 예외처리
    @ExceptionHandler({AutocompleteException.class, AutocompleteException.class})
    public ResponseEntity<ErrorResponse> handleRequestLimitExceededException(AutocompleteException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
    }
}
