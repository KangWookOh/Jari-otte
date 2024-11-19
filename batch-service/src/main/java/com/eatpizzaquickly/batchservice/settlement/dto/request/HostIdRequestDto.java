package com.eatpizzaquickly.batchservice.settlement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HostIdRequestDto {
    private HashSet<Long> concertIds;
}
