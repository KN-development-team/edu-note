package com.edu.edu_note.domain.user.entity;

import com.edu.edu_note.domain.user.enums.Level;
import com.edu.edu_note.domain.user.enums.Role;
import com.edu.edu_note.domain.user.enums.UserStatus;
import com.edu.edu_note.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users") // DB 예약어 user와 겹칠 수 있어 users로 명시 추천
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Level level;

    @Builder
    public User(String name, String password, String email, Role role) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = (role != null) ? role : Role.USER; // 요청 없으면 기본 USER
        this.status = UserStatus.ACTIVE; // 기본값
        this.level = Level.BASIC;       // 기본값
    }
}