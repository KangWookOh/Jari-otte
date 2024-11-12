package com.eatpizzaquickly.batchservice.settlement.reader;


import com.eatpizzaquickly.batchservice.settlement.entity.HostPoint;
import com.eatpizzaquickly.batchservice.settlement.repository.HostPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.eatpizzaquickly.batchservice.settlement.common.BatchConstant.CHUNK_SIZE;

@RequiredArgsConstructor
@Component
public class HostPointReader {
    private final HostPointRepository hostPointRepository;
    public RepositoryItemReader<HostPoint> hostPointReader() {
        return new RepositoryItemReaderBuilder<HostPoint>()
                .name("hostPointReader")
                .repository(hostPointRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }
}
