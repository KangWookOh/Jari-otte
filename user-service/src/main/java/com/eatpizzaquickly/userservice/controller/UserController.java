package com.eatpizzaquickly.userservice.controller;


import com.eatpizzaquickly.userservice.common.advice.ApiResponse;

import com.eatpizzaquickly.userservice.dto.UserRequestDto;
import com.eatpizzaquickly.userservice.dto.UserResponseDto;
import com.eatpizzaquickly.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;


    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> signUp(@Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto user = userService.signUp(userRequestDto);
        return ResponseEntity.ok(ApiResponse.success("회원가입 성공 ", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody UserRequestDto userRequestDto) {
        String accessToken = userService.login(userRequestDto);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)  // 헤더에 액세스 토큰 추가
                .body(ApiResponse.success("로그인 성공", accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
        return ResponseEntity.ok(new ApiResponse<>("로그아웃 성공"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUsers(@RequestHeader("X-Authenticated-User") Long userId) {
        UserResponseDto user = userService.myPage(userId);
        return ResponseEntity.ok(ApiResponse.success("마이페이지 조회 성공",user));
    }


    @PatchMapping("/my")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@RequestHeader("X-Authenticated-User") Long userId,
                                                                   @Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto user = userService.updateUser(userId, userRequestDto);
        return ResponseEntity.ok(ApiResponse.success("수정 성공 ",user));
    }

    @PatchMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@RequestHeader("X-Authenticated-User") Long userId,
                                                          @Valid @RequestBody UserRequestDto userRequestDto) {
         userService.deleteUser(userId,userRequestDto.getPassword());
         return ResponseEntity.ok(ApiResponse.success("탈퇴성공"));


    }

}
