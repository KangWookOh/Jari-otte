package com.eatpizzaquickly.batchservice.settlement.config;

import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class PagingQueryProviderConfig {

    @Bean
    public PagingQueryProvider pagingQueryProvider(){
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("SELECT id, payment_id, settlement_status, pay_status, amount, concert_id");
        queryProvider.setFromClause("FROM temp_payment");
        queryProvider.setWhereClause("WHERE settlement_status = :status");
        queryProvider.setSortKeys(Collections.singletonMap("id", Order.ASCENDING)); // 정렬 기준 컬럼

        return queryProvider;
    }
}
