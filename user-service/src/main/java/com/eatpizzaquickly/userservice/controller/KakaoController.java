package com.eatpizzaquickly.userservice.controller;

import com.eatpizzaquickly.userservice.common.advice.ApiResponse;
import com.eatpizzaquickly.userservice.dto.KakaoUserDto;
import com.eatpizzaquickly.userservice.service.KakaoService;
import com.eatpizzaquickly.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class KakaoController {
    private final KakaoService kakaoService;
    private final UserService userService;
    @Value("${kakao.client_id}")
    private String client_id;

    @Value("${kakao.redirect_uri}")
    private String redirect_uri;

    @GetMapping("/oauth/kakao")
    public String loginPage(Model model) {
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+client_id+"&redirect_uri="+redirect_uri;
        model.addAttribute("location", location);

        return "kakao_login";
    }

    @GetMapping("/oauth/kakao/callback")
    public String callback(@RequestParam("code") String code) {
        // 1. 카카오 토큰 발급
        String kakaoToken = kakaoService.getAccessToken(code);

        // 2. 카카오 유저 정보 가져오기
        KakaoUserDto kakaoUser = kakaoService.getKakaoUser(kakaoToken);

        // 3. 로그인 또는 회원가입 처리 후 JWT 발급
        String jwtToken = userService.kakaoLogin(kakaoUser);

        // 4. 리다이렉트 URL 구성
        String redirectUrl = "http://localhost:5173/?token=" + jwtToken;

        // 5. 리다이렉트
        return "redirect:" + redirectUrl;
    }

}
