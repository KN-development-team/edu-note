package com.edu.edu_note.domain.user.dto;

import com.edu.edu_note.domain.user.entity.User;
import com.edu.edu_note.domain.user.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignUpRequest {
    private String name;
    private String password;
    private String email;
    private Role role;    // "USER" 문자열이 들어오면 자동으로 Enum 변환됨

    public User toEntity(String encodedPassword) {
        return User.builder()
                .name(this.name)
                .password(encodedPassword)
                .email(this.email)
                .role(this.role)
                .build();
    }
}