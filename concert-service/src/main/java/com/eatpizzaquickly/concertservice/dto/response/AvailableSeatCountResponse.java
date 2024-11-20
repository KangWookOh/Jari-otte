package com.eatpizzaquickly.concertservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AvailableSeatCountResponse {
    private Integer availableSeatCount;

    public static AvailableSeatCountResponse of(Integer availableSeatCount) {
        return new AvailableSeatCountResponse(availableSeatCount);
    }
}
