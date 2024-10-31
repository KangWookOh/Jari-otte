package com.eatpizzaquickly.userservice.service;

import com.eatpizzaquickly.userservice.common.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final RedisConfig redisConfig;
    private final RedisTemplate<String, String> redisTemplate;

    @Async("emailExecutor")
    public void sendMail(String email) {
        Random random = new Random();
        String token = String.format("%06d", random.nextInt(1000000));
        StringBuilder stringBuilder = new StringBuilder();
        // 3분 동안 redis에 보관
        redisTemplate.opsForValue().set(email, token, 3, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Jari-Otte 이메일 인증 메일");
        message.setText("Jari-Otte 인증 번호\n" + token);

        javaMailSender.send(message);
        log.info(email + "에 인증 메일 발송");
    }
}
