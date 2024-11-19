package com.eatpizzaquickly.concertservice.controller;

import com.eatpizzaquickly.concertservice.dto.response.ApiResponse;
import com.eatpizzaquickly.concertservice.dto.response.QueueResponse;
import com.eatpizzaquickly.concertservice.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/concerts")
@RestController
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;

    @GetMapping("/{concertId}/queue")
    public ResponseEntity<ApiResponse<QueueResponse>> getWaitingQueuePosition(@PathVariable Long concertId,
                                                                              @RequestHeader("X-Authenticated-User") Long userId) {
        QueueResponse queueResponse = waitingQueueService.findPosition(concertId, userId);
        return ResponseEntity.ok(ApiResponse.success("대기열 조회 성공", queueResponse));
    }

    @GetMapping("/{concertId}/queue/process")
    public ResponseEntity<ApiResponse<Void>> processWaitingQueue(@PathVariable Long concertId) {
        waitingQueueService.processQueue(concertId);
        return ResponseEntity.ok(ApiResponse.success("대기열 작업 성공"));
    }
}
