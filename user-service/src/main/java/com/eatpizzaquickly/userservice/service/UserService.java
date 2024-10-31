package com.eatpizzaquickly.userservice.service;


import com.eatpizzaquickly.userservice.common.config.JwtUtils;
import com.eatpizzaquickly.userservice.common.config.PasswordEncoder;
import com.eatpizzaquickly.userservice.dto.UserRequestDto;
import com.eatpizzaquickly.userservice.dto.UserResponseDto;
import com.eatpizzaquickly.userservice.entity.User;
import com.eatpizzaquickly.userservice.entity.UserRole;
import com.eatpizzaquickly.userservice.exception.*;
import com.eatpizzaquickly.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    @Transactional
    public UserResponseDto signUp(UserRequestDto userRequestDto, String token) {
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new DuplicateUserException("이미 가입된 유저입니다.");
        }

        verifyToken(userRequestDto.getEmail(), token);

        String password = passwordEncoder.encode(userRequestDto.getPassword());
        UserRole role = Optional.ofNullable(userRequestDto.getUserRole()).orElse(UserRole.USER);

        User user = User.builder()
                .email(userRequestDto.getEmail())
                .password(password)
                .nickname(userRequestDto.getNickname())
                .userRole(role)
                .build();
        user = userRepository.save(user);
        return UserResponseDto.from(user);
    }

    @Transactional
    public String login(UserRequestDto userRequestDto) {
        User user = userRepository.findByEmail(userRequestDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(userRequestDto.getPassword(), user.getPassword())) {
            throw new PasswordNotMatchException("잘못된 비밀번호 입니다.");
        }

        String accessToken = jwtUtils.createToken(user.getId(), user.getUserRole());
        String refreshToken = jwtUtils.createRefreshToken(user.getId(), user.getUserRole());
        // Redis에 RefreshToken 저장
        redisTemplate.opsForValue().set(
                "RT:" + user.getEmail(),
                refreshToken,
                jwtUtils.getRefreshTokenExpirationTime(),
                TimeUnit.MILLISECONDS
        );
        return accessToken;
    }

    public String logout(String accessToken) {
        String email = jwtUtils.getUserIdFromToken(accessToken);
        redisTemplate.delete("RT:" + email);
        jwtUtils.invalidToken(accessToken);
        return null;
    }


    @Cacheable(value = "userById", key = "#id", cacheManager = "redisCacheManager")
    public UserResponseDto myPage(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        String newPassword = null;
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을수 없습니다."));
        // 비밀번호가 동일하지 않으면 새 비밀번호로 인코딩, 동일하면 기존 비밀번호 유지
        if (!passwordEncoder.matches(userRequestDto.getPassword(), user.getPassword())) {
            newPassword = passwordEncoder.encode(userRequestDto.getPassword());
        } else {
            newPassword = user.getPassword();  // 기존 비밀번호 유지
        }
        user.updateUser(newPassword, userRequestDto.getNickname());
        user = userRepository.save(user);
        return UserResponseDto.from(user);
    }

    @Transactional
    public void deleteUser(Long id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new PasswordNotMatchException("유저를 찾을수 없습니다"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new PasswordNotMatchException("비밀번호가 일치 하지않습니다.");
        }
        user.deleteAccount();
    }

    @Cacheable(value = "userById", key = "#id", cacheManager = "redisCacheManager")
    public UserResponseDto findById(Long userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(()->new UserNotFoundException("유저를 찾을 수 없습니다."));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void verifyToken(String email, String token) {
        String verifyToken = redisTemplate.opsForValue().get(email);

        if (verifyToken == null) {
            emailService.sendMail(email);
            throw new VerifyExpiredException("만료된 인증 번호입니다. 재발송된 번호를 다시 입력해주세요.");
        }
        if (!token.equals(verifyToken)) {
            throw new TokenNotMatchException("인증 실패! 인증 번호를 다시 입력해주세요");
        }
        log.info(email + " 인증 확인");
    }
}
