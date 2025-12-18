package com.edu.edu_note.domain.user.dto;

import com.edu.edu_note.domain.user.entity.User;
import com.edu.edu_note.domain.user.enums.Level;
import com.edu.edu_note.domain.user.enums.Role;
import com.edu.edu_note.domain.user.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserSignUpResponse {
    private Long userId;
    private String name;
    private String email;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Role role;
    private Level level;

    // Entity -> Response DTO 변환 메서드
    public static UserSignUpResponse from(User user) {
        return UserSignUpResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .role(user.getRole())
                .level(user.getLevel())
                .build();
    }
}