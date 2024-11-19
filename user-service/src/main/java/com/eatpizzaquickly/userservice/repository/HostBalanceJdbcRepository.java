package com.eatpizzaquickly.userservice.repository;

import com.eatpizzaquickly.userservice.dto.HostPointRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HostBalanceJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public void batchInsertHostBalance(List<HostPointRequestDto> hostPoints) {
        String sql = "INSERT INTO host_balance (host_id, balance) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE balance = balance + VALUES(balance)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                HostPointRequestDto hostPoint = hostPoints.get(i);
                ps.setLong(1, hostPoint.getHostId());
                ps.setLong(2, hostPoint.getPoints());
            }

            @Override
            public int getBatchSize() {
                return hostPoints.size();
            }
        });
    }
}
