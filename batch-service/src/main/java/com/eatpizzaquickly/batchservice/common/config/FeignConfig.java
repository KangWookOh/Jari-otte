package com.eatpizzaquickly.batchservice.common.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    @Bean
    public RequestInterceptor requestLoggingInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 요청 경로와 HTTP 메서드 로깅
                log.info("Feign Request to URI: {} Method: {}", template.url(), template.method());

                // 요청 바디가 있다면 바디 로깅
                if (template.body() != null) {
                    log.info("Request Body: {}", new String(template.body()));
                }
            }
        };
    }
    @Bean
    public Request.Options options() {
        return new Request.Options(
                5000,  // connectTimeout (5 seconds)
                TimeUnit.MILLISECONDS,
                30000, // readTimeout (30 seconds)
                TimeUnit.MILLISECONDS,
                true   // followRedirects
        );
    }
}