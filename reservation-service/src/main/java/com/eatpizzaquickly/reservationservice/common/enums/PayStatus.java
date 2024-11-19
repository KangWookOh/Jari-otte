package com.eatpizzaquickly.reservationservice.common.enums;


import lombok.Getter;

@Getter
public enum PayStatus {
    READY, PAID, CANCELLED,FAILED;
}
