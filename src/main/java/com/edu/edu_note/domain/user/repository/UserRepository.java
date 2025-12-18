package com.edu.edu_note.domain.user.repository;

import com.edu.edu_note.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 중복 가입 방지를 위한 조회 메서드
    // SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);}