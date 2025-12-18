package com.edu.edu_note.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보안 비활성화 (API 서버는 보통 끔)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 요청 주소별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 로그인은 누구나 접속 가능하게 허용
                        .requestMatchers("/api/users/signup", "/api/users/login").permitAll()

                        // 그 외 모든 요청은 인증(로그인) 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}