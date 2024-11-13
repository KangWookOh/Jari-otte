package com.eatpizzaquickly.batchservice.settlement.writer;


import com.eatpizzaquickly.batchservice.common.client.UserClient;
import com.eatpizzaquickly.batchservice.settlement.dto.request.HostPointRequestDto;
import com.eatpizzaquickly.batchservice.settlement.entity.HostPoint;
import com.eatpizzaquickly.batchservice.settlement.repository.HostPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.naming.ServiceUnavailableException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class HostPointWriter {
    private final HostPointRepository hostPointRepository;
    private final UserClient userClient;

    public ItemWriter<HostPoint> hostPointWriter() {
        return hostPointRepository::saveAll;
    }

    public ItemWriter<HostPointRequestDto> hostPointTransmissionWriter() {
        return hostPoints -> {
            ResponseEntity<String> response = userClient.addPointsToHost((List<HostPointRequestDto>) hostPoints.getItems());
            log.info("포인트 추가 응답 STATUS {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("호스트 포인트 전송 완료");
            } else {
                // 실패 시 예외 처리 또는 재시도 로직 추가 가능
                log.error("포인트 전송 실패: {}", response.getStatusCode());
                throw new ServiceUnavailableException("포인트 전송에 실패했습니다.");
            }
        };
    }
}
