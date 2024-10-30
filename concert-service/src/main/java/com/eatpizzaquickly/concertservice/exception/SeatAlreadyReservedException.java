package com.eatpizzaquickly.concertservice.exception;

public class SeatAlreadyReservedException extends BadRequestException{
    private static final String MESSAGE = "이미 예약된 좌석입니다.";

    public SeatAlreadyReservedException() {super(MESSAGE);}

    public SeatAlreadyReservedException(String message) {super(message);}
}
