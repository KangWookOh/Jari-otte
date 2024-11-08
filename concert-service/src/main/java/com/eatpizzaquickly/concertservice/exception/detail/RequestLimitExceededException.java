package com.eatpizzaquickly.concertservice.exception.detail;

public class RequestLimitExceededException extends RuntimeException{
    private static final String MESSAGE = "/api/v1/queue/position";

    public RequestLimitExceededException() {super(MESSAGE);}

    public RequestLimitExceededException(String message) {super(message);}
}
