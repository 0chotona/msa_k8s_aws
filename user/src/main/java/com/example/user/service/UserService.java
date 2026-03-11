package com.example.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user.dao.UserRepository;
import com.example.user.domain.dto.UserRequestDto;
import com.example.user.domain.dto.UserResponseDto;
import com.example.user.domain.entity.UserEntity;
import com.example.user.provider.JwtProvider;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // redis
    @Qualifier("tokenRedis")
    private final RedisTemplate<String, Object> redisTemplate;

    private static final long REFRESH_TOKEN_TTL = 60 * 60 * 24 * 7; // 7일

    public Map<String, Object> signIn(UserRequestDto request) {
        System.out.println(">>> user service signin");
        Map<String, Object> map = new HashMap<>();

        // plain version
        // System.out.println(">>> 1. user service 사용자 조회");
        // UserEntity entity = userRepository.findByEmailAndPassword(request.getEmail(),
        // request.getPassword())
        // .orElseThrow(() -> new RuntimeException("※ 존재하지 않는 회원"));

        // hashing version
        UserEntity entity = userRepository.findById(request.getEmail())
                .orElseThrow(() -> new RuntimeException("※ 존재하지 않는 회원"));
        System.out.println(">>> user service request : " + request.getPassword());
        System.out.println(">>> user service entity : " + entity.getPassword());
        // plain vs encoded
        if (!passwordEncoder.matches(request.getPassword(), entity.getPassword())) {
            throw new RuntimeException("비밀번호가 맞지 않음");
        }

        System.out.println(">>> 2. user service 토큰 생성");
        String at = jwtProvider.createRT(entity.getEmail());
        String rt = jwtProvider.createRT(entity.getEmail());

        System.out.println(">>> 3. user service RT토큰 Redis 저장");
        redisTemplate.opsForValue()
                .set("RT:" + entity.getEmail(), rt, REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        map.put("response", UserResponseDto.fromEntity(entity));
        map.put("access", at);
        map.put("refresh", rt);

        return map;
    }
}
