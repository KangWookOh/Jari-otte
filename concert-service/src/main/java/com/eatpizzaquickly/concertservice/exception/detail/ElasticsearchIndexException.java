package com.eatpizzaquickly.concertservice.exception.detail;

public class ElasticsearchIndexException extends RuntimeException {

    public ElasticsearchIndexException(String message) {
        super(message);
    }
    // 새로 추가: Throwable을 받는 생성자
    public ElasticsearchIndexException(String message, Throwable cause) {
        super(message, cause);
    }
}
