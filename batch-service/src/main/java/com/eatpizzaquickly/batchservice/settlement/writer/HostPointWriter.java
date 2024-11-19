package com.eatpizzaquickly.batchservice.settlement.writer;


import com.eatpizzaquickly.batchservice.common.client.ConcertClient;
import com.eatpizzaquickly.batchservice.common.client.UserClient;
import com.eatpizzaquickly.batchservice.settlement.dto.request.HostIdRequestDto;
import com.eatpizzaquickly.batchservice.settlement.dto.request.HostPointRequestDto;
import com.eatpizzaquickly.batchservice.settlement.dto.response.ConcertHostResponseDto;
import com.eatpizzaquickly.batchservice.settlement.entity.HostPoint;
import com.eatpizzaquickly.batchservice.settlement.entity.TempPayment;
import com.eatpizzaquickly.batchservice.settlement.repository.HostPointRepository;
import com.eatpizzaquickly.batchservice.settlement.repository.TempPaymentJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.naming.ServiceUnavailableException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.eatpizzaquickly.batchservice.settlement.common.BatchConstant.COMMISSION;

@Slf4j
@RequiredArgsConstructor
@Component
public class HostPointWriter {
    private final HostPointRepository hostPointRepository;
    private final TempPaymentJdbcRepository tempPaymentRepository;
    private final UserClient userClient;
    private final ConcertClient concertClient;
    private final JdbcTemplate jdbcTemplate;

    public ItemWriter<TempPayment> hostPointWriter() {
        return tempPayments -> {
            HashSet<Long> concertIds = tempPayments.getItems().stream()
                    .map(TempPayment::getConcertId)
                    .collect(Collectors.toCollection(HashSet::new));
            HostIdRequestDto requestDto = new HostIdRequestDto(concertIds);
            ResponseEntity<ConcertHostResponseDto> concertResponse = concertClient.findHostIdsByConcertIds(requestDto);
            log.info("콘서트 feign 응답코드 : {}", concertResponse.getStatusCode());
            Map<String, Long> hostIds = concertResponse.getBody().getResult();

            List<HostPoint> hostPoints = tempPayments.getItems().stream()
                    .map(payment -> {
                        Long payId = payment.getPaymentId();
                        Long hostId = hostIds.get(payment.getConcertId().toString());
                        Long points = calculatePoints(payment.getAmount()); // 수수료 떼고 정산
                        return new HostPoint(payId, hostId, points);
                    })
                    .toList();

            String sql = "INSERT INTO host_point (host_id, pay_id, points) " +
                    "VALUES (?, ?, ?) ";
            jdbcTemplate.batchUpdate(sql,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            HostPoint hostPoint = hostPoints.get(i);
                            ps.setLong(1, hostPoint.getHostId());
                            ps.setLong(2, hostPoint.getPayId());
                            ps.setLong(3, hostPoint.getPoints());
                        }

                        @Override
                        public int getBatchSize() {
                            return hostPoints.size();
                        }
                    });
        };
    }

    public ItemWriter<HostPointRequestDto> hostPointTransmissionWriter() {
        return hostPoints -> {
            ResponseEntity<String> response = userClient.addPointsToHost((List<HostPointRequestDto>) hostPoints.getItems());
            log.info("포인트 추가 응답 STATUS {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("호스트 포인트 전송 완료");
                List<Long> ids = hostPoints.getItems().stream()
                        .map(HostPointRequestDto::getPayId)
                        .toList();
                int result = tempPaymentRepository.updateSettlementStatus(ids);
                log.info("정산 상태 변경 , ids : {} {}", result, ids);
            } else {
                // 실패 시 예외 처리 또는 재시도 로직 추가 가능
                log.error("포인트 전송 실패: {}", response.getStatusCode());
                throw new ServiceUnavailableException("포인트 전송에 실패했습니다.");
            }
        };
    }

    Long calculatePoints(Long amount) {
        return (long) (amount * COMMISSION);
    }
}
