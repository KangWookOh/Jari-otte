package com.eatpizzaquickly.reservationservice.review.client.user;

import lombok.Getter;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String email;
    private final String nickname;


    public UserResponseDto(Long id,String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }
}
