package com.eatpizzaquickly.batchservice.settlement.processor;

import com.eatpizzaquickly.reservationservice.payment.dto.request.HostPointRequestDto;
import com.eatpizzaquickly.reservationservice.payment.entity.HostPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class HostPointProcessor {
    public ItemProcessor<HostPoint, HostPointRequestDto> pointTransmissionProcessor() {
        return HostPointRequestDto::from;
    }
}
