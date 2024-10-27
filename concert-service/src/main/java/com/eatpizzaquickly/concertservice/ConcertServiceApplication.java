package com.eatpizzaquickly.concertservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ConcertServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcertServiceApplication.class, args);
    }

}
