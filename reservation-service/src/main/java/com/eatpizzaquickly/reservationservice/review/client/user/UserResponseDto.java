package com.eatpizzaquickly.reservationservice.review.client.user;

import com.eatpizzaquickly.reservationservice.review.dto.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private UserRole userRole;


    public UserResponseDto(Long id,String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }
}
