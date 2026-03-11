package com.example.user.domain.dto;

import com.example.user.domain.entity.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    String email;
    String name;
    String role;

    public static UserResponseDto fromEntity(UserEntity entity) {
        return UserResponseDto.builder()
                .email(entity.getEmail())
                .name(entity.getName())
                .role(entity.getRole())
                .build();
    }
}
