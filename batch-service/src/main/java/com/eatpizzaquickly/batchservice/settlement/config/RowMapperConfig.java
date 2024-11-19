package com.eatpizzaquickly.batchservice.settlement.config;

import com.eatpizzaquickly.batchservice.settlement.entity.PayStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.SettlementStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.TempPayment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
public class RowMapperConfig {

    @Bean
    public RowMapper<TempPayment> tempPaymentRowMapper() {
        return new RowMapper<TempPayment>() {
            @Override
            public TempPayment mapRow(ResultSet rs, int rowNum) throws SQLException {
                TempPayment tempPayment = new TempPayment();
                tempPayment.setId(rs.getLong("id"));
                tempPayment.setPaymentId(rs.getLong("payment_id"));
                tempPayment.setSettlementStatus(SettlementStatus.valueOf(rs.getString("settlement_status")));
                tempPayment.setPayStatus(PayStatus.valueOf(rs.getString("pay_status")));
                tempPayment.setAmount(rs.getLong("amount"));
                tempPayment.setConcertId(rs.getLong("concert_id"));
                return tempPayment;
            }
        };
    }
}
