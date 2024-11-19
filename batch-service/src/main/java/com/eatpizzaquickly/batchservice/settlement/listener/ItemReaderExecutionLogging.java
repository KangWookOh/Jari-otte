package com.eatpizzaquickly.batchservice.settlement.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;

@Slf4j
public class ItemReaderExecutionLogging<T> implements ItemReadListener<T> {
    private long totalReadTime = 0L;
    private long readStartTime = 0L;

    @Override
    public void beforeRead() {
        readStartTime = System.currentTimeMillis();
    }

    @Override
    public void afterRead(T item) {
        totalReadTime += (System.currentTimeMillis() - readStartTime);
        log.info("총 Reader 수행 시간: {} ms", totalReadTime);
    }
}
