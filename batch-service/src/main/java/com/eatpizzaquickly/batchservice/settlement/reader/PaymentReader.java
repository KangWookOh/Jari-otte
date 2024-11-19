package com.eatpizzaquickly.batchservice.settlement.reader;

import com.eatpizzaquickly.batchservice.common.client.PaymentClient;
import com.eatpizzaquickly.batchservice.common.exception.NotFoundException;
import com.eatpizzaquickly.batchservice.settlement.config.RowMapperConfig;
import com.eatpizzaquickly.batchservice.settlement.dto.response.PaymentResponseDto;
import com.eatpizzaquickly.batchservice.settlement.entity.PayStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.SettlementStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.TempPayment;
import com.eatpizzaquickly.batchservice.settlement.repository.TempPaymentRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.eatpizzaquickly.batchservice.settlement.common.BatchConstant.CHUNK_SIZE;

@RequiredArgsConstructor
@Component
@Slf4j
public class PaymentReader {

    private final PaymentClient paymentClient;
    private final TempPaymentRepository tempPaymentRepository;
    private final EntityManagerFactory entityManagerFactory;
    public static final String OFFSET_KEY = "current_offset";
    private final RedisTemplate<String, String> redisTemplate;
    private final DataSource dataSource;
    private final PagingQueryProvider pagingQueryProvider;
    private final RowMapperConfig rowMapperConfig;

    public ItemReader<PaymentResponseDto> paidPaymentReader() {
        return new ItemReader<PaymentResponseDto>() {
            private List<PaymentResponseDto> currentChunk = new ArrayList<>();

            @BeforeStep
            public void initializeOffset(StepExecution stepExecution) {
                // 배치 작업 시작 시, Redis에 키가 없으면 초기값 설정
                Boolean hasKey = redisTemplate.hasKey(OFFSET_KEY);
                if (Boolean.FALSE.equals(hasKey)) {
                    redisTemplate.opsForValue().set(OFFSET_KEY, "0", 60000, TimeUnit.MILLISECONDS);
                    log.info("Offset Initialize");
                }
            }

            @Override
            public PaymentResponseDto read() {
                // currentChunk가 비었을 경우 새로운 데이터를 가져옴
                if (currentChunk.isEmpty()) {
                    Long currentOffset = Long.valueOf(redisTemplate.opsForValue().get(OFFSET_KEY));

                    // ID를 기준으로 하는 ZeroOffsetReader로 변경
                    currentChunk = paymentClient.getPaymentsByStatusAfterId(
                            SettlementStatus.UNSETTLED, PayStatus.PAID, CHUNK_SIZE, currentOffset);

                    // 데이터가 없으면 null 반환
                    if (currentChunk.isEmpty()) {
                        log.info("Offset {} 데이터 처리 완료", currentOffset);
                        return null;
                    }

                    Long maxPaymentId = currentChunk.stream()
                            .mapToLong(PaymentResponseDto::getId)
                            .max()
                            .orElseThrow(() -> new NotFoundException("진행할 payment가 없습니다."));

                    redisTemplate.opsForValue().set(OFFSET_KEY, String.valueOf(maxPaymentId), 60000, TimeUnit.MILLISECONDS);
                }

                // 첫 번째 데이터를 반환하고, 리스트에서 제거
                PaymentResponseDto paymentResponseDto = currentChunk.remove(0);
                log.info("Processing PaymentResponseDto: {}", paymentResponseDto);
                return paymentResponseDto;
            }
        };
    }

    @Bean
    public JdbcPagingItemReader<TempPayment> pointAdditionReader() {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("status", SettlementStatus.PROGRESS.name());

        return new JdbcPagingItemReaderBuilder<TempPayment>()
                .name("pointAdditionReader")
                .dataSource(dataSource)
                .fetchSize(CHUNK_SIZE)
                .pageSize(CHUNK_SIZE) // 페이지 크기 조정
                .queryProvider(pagingQueryProvider)
                .parameterValues(parameterValues)
                .rowMapper(rowMapperConfig.tempPaymentRowMapper())
                .saveState(true) // 상태 저장 여부
                .build();
    }


    @Bean
    public JpaPagingItemReader<TempPayment> updatePaymentReader() {
        return createReader("updatePaymentReader", SettlementStatus.SETTLED);
    }

    @Bean
    public JpaPagingItemReader<TempPayment> settlementSettledReader() {
        return createReader("settlementSettledReader", SettlementStatus.PROGRESS);
    }

    private JpaPagingItemReader<TempPayment> createReader(String name, SettlementStatus status) {
        return new JpaPagingItemReaderBuilder<TempPayment>()
                .name(name)
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT t FROM TempPayment t WHERE t.settlementStatus = :status ORDER BY t.id ASC")
                .parameterValues(Collections.singletonMap("status", status))
                .build();
    }

}
