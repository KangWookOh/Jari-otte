package com.eatpizzaquickly.batchservice.settlement.Listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StepLoggingListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("step start {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("step completed {}", stepExecution.getStepName());
        log.info("read count {}", stepExecution.getReadCount());
        log.info("write count {}", stepExecution.getWriteCount());
        log.info("commit count {}", stepExecution.getCommitCount());
        log.info("skip count {}", stepExecution.getSkipCount());
        log.info("step exit status {}", stepExecution.getExitStatus());
        return stepExecution.getExitStatus();
    }
}
