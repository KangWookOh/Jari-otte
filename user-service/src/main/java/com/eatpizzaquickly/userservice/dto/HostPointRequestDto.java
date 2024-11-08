package com.eatpizzaquickly.userservice.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class HostPointRequestDto {
    private List<HostPoint> hostPoints;

    @Getter
    public static class HostPoint {
        private Long hostId;
        private Long points;
    }
}
