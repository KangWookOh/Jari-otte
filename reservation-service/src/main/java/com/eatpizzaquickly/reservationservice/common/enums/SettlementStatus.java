package com.eatpizzaquickly.reservationservice.common.enums;

import lombok.Getter;

@Getter
public enum SettlementStatus {
    UNSETTLED,      // 정산되지 않음
    PROGRESS,    // 정산 중 (포인트 지급 전)
    SETTLED       // 정산 완료 (포인트 지급 후)
}
