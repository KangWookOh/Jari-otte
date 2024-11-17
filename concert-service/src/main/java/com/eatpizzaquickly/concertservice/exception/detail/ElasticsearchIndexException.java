package com.eatpizzaquickly.concertservice.exception.detail;

public class ElasticsearchIndexException extends RuntimeException {

    public ElasticsearchIndexException(String message) {
        super(message);
    }
}
