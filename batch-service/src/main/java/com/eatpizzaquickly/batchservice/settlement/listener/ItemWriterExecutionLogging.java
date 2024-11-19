package com.eatpizzaquickly.batchservice.settlement.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;

@Slf4j
public class ItemWriterExecutionLogging<T> implements ItemWriteListener<T> {
    private Long totalWriteTime = 0L;
    private Long writeStartTime = 0L;

    @Override
    public void beforeWrite(Chunk<? extends T> items) {
        writeStartTime = System.currentTimeMillis();
    }

    @Override
    public void afterWrite(Chunk<? extends T> items) {
        totalWriteTime += (System.currentTimeMillis() - writeStartTime);
        log.info("총 Writer 수행 시간: {} ms", totalWriteTime);
    }
}
