package com.eatpizzaquickly.reservationservice.review.entity;

import com.eatpizzaquickly.reservationservice.review.dto.ReviewRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer rating;
    private String content;
    private String nickname;

    @Column(name = "user_email")
    private String userEmail;

    // 콘서트 ID(FK)
    @Column(name = "concert_id")
    private Long concertId;

    // 유저 ID (FK)
    @Column(name = "user_id")
    private Long userId;

    // 생성일
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @Builder
    public Review(Integer rating, String content, String nickname, String userEmail, Long userId, Long concertId, LocalDateTime createdAt, LocalDateTime modifiedDate) {
        this.rating = rating;
        this.content = content;
        this.nickname = nickname;
        this.userEmail = userEmail;
        this.userId = userId;
        this.concertId = concertId;
        this.createdAt = createdAt;
        this.modifiedDate = modifiedDate;
    }

    public void update(ReviewRequestDto requestDto) {
        if (requestDto.getRating() != null) {
            this.rating = requestDto.getRating();
        }
        this.content = requestDto.getContent();
    }
}
