package com.example.user.ctrl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user.domain.dto.UserRequestDto;
import com.example.user.domain.dto.UserResponseDto;
import com.example.user.service.UserService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users") // "api/v1/auth/users" 이게 네이밍 정석
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestBody UserRequestDto request) {
        System.out.println(">>> user ctrl path : /login");
        System.out.println("params : " + request);

        Map<String, Object> map = userService.signIn(request);

        System.out.println((UserResponseDto) (map.get("response")));
        System.out.println((String) (map.get("access")));
        System.out.println((String) (map.get("refresh")));

        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + (String) (map.get("access")));
        headers.add("Refresh-Token", (String) (map.get("refresh")));
        headers.add("Access-Control-Expose-Headers", "Authorization, Refresh-Token");
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body((String) (map.get("access")));
    }

}
