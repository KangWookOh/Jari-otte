package com.eatpizzaquickly.batchservice.common.client;

import com.eatpizzaquickly.batchservice.settlement.dto.request.PaymentRequestDto;
import com.eatpizzaquickly.batchservice.settlement.dto.response.PaymentResponseDto;
import com.eatpizzaquickly.batchservice.settlement.entity.PayStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.SettlementStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@FeignClient(name = "reservation-service")
public interface PaymentClient {

    String CIRCUIT_BREAKER_NAME = "paymentService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getPaymentsByStatusAfterIdFallback")
    @GetMapping("/api/v1/payments")
    List<PaymentResponseDto> getPaymentsByStatusAfterId(
            @RequestParam(name = "settlementStatus") SettlementStatus settlementStatus,
            @RequestParam(name = "payStatus") PayStatus payStatus,
            @RequestParam(name = "size") int chunk,
            @RequestParam(name = "offset") Long currentOffset);

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "updatePaymentsFallback")
    @PutMapping("/api/v1/payments")
    ResponseEntity<String> updatePayments(@RequestBody List<PaymentRequestDto> payments);

    // Fallback methods
    default List<PaymentResponseDto> getPaymentsByStatusAfterIdFallback(
            SettlementStatus settlementStatus,
            PayStatus payStatus,
            int chunk,
            Long currentOffset,
            Exception ex) {
        // 조회 실패 시 빈 리스트 반환
        return new ArrayList<>();
    }

    default ResponseEntity<String> updatePaymentsFallback(List<PaymentRequestDto> payments, Exception ex) {
        return ResponseEntity.internalServerError()
                .body("Failed to update payments. Will retry in next batch.");
    }
}