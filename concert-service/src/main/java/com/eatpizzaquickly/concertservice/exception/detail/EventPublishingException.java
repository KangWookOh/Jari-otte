package com.eatpizzaquickly.concertservice.exception.detail;

public class EventPublishingException extends RuntimeException{
    private static final String MESSAGE = "이벤트 발행중 문제가 발생했습니다.";

    public EventPublishingException() {super(MESSAGE);}

    public EventPublishingException(String message) {super(message);}
}
