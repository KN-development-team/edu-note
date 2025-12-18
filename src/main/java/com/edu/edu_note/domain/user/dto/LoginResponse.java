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
public class LoginResponse {
    private String access_token;
    private String refresh_token;
    private Long userId;
    private String name;
    private String email;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Role role;
    private Level level;

    // User 엔티티와 토큰들을 받아서 응답 DTO로 변환
    public static LoginResponse of(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken)
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