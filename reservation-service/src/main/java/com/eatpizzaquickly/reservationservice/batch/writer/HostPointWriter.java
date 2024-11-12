package com.eatpizzaquickly.reservationservice.batch.writer;

import com.eatpizzaquickly.reservationservice.payment.client.UserClient;
import com.eatpizzaquickly.reservationservice.payment.dto.request.HostPointRequestDto;
import com.eatpizzaquickly.reservationservice.payment.entity.HostPoint;
import com.eatpizzaquickly.reservationservice.payment.repository.HostPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class HostPointWriter {
    private final HostPointRepository hostPointRepository;
    private final UserClient userClient;

    public ItemWriter<HostPoint> hostPointWriter() {
        return HostPoint -> {
            hostPointRepository.saveAll(HostPoint);
            log.info("중간 결과 저장");
        };
    }

    public ItemWriter<HostPointRequestDto> hostPointTransmissionWriter() {
        return hostPoints -> {
            ResponseEntity<String> response = userClient.addPointsToHost((List<HostPointRequestDto>) hostPoints.getItems());
            log.info("포인트 추가 응답 STATUS {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                // 전송이 성공하면 HostPoint 엔티티 삭제
                List<Long> hostIds = ((List<HostPointRequestDto>) hostPoints.getItems()).stream()
                        .map(HostPointRequestDto::getHostId)
                        .collect(Collectors.toList());

                List<HostPoint> hostPointsToDelete = hostPointRepository.findByHostIdIn(hostIds);
                hostPointRepository.deleteAll(hostPointsToDelete);
                log.info("호스트 포인트 전송 완료 및 삭제 완료");
            } else {
                // 실패 시 예외 처리 또는 재시도 로직 추가 가능
                log.error("포인트 전송 실패: {}", response.getStatusCode());
                throw new ServiceUnavailableException("포인트 전송에 실패했습니다.");
            }
        };
    }
}
