package com.eatpizzaquickly.concertservice.dto.request;

import lombok.Getter;

@Getter
public class ConcertUpdateRequest {
    private String title;
    private String description;
    private String thumbnailUrl;
}
