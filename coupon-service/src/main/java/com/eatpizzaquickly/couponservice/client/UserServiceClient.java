package com.eatpizzaquickly.couponservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// UserServiceClient.java
@FeignClient(name = "user-service", url = "${gateway.url}")
public interface UserServiceClient {

    @GetMapping("/api/users/check")
    Boolean checkUserExists(@RequestParam("id") Long id);
}
