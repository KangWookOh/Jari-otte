package com.eatpizzaquickly.userservice.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Boolean isDelete = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column(nullable = true, unique = true)
    private Long kakaoId;

    @Builder
    public User(String email, String password, String nickname, UserRole userRole, Long kakaoId) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.userRole = userRole;
        this.kakaoId = kakaoId;
    }

    public void deleteAccount() {
        this.isDelete = true;
    }

    public void updateUser(String password, String nickname) {
        this.password = password;
        this.nickname = nickname;
    }

    public void setKakaoId(Long kakaoId) {
        this.kakaoId = kakaoId;
    }
}