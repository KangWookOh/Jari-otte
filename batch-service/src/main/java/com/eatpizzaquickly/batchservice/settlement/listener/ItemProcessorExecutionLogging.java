package com.eatpizzaquickly.batchservice.settlement.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;

@Slf4j
public class ItemProcessorExecutionLogging<T> implements ItemProcessListener<T, T> {
    private Long totalProcessTime = 0L;
    private Long processStartTime = 0L;
    @Override
    public void beforeProcess(T item) {
        processStartTime = System.currentTimeMillis();
    }

    @Override
    public void afterProcess(T item, T result) {
        totalProcessTime += (System.currentTimeMillis() - processStartTime);
        log.info("총 Processor 수행 시간: {} ms", totalProcessTime);
    }
}
