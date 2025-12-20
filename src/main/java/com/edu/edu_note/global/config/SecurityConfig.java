package com.edu.edu_note.global.config;

import com.edu.edu_note.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // 1. CSRF 보안 비활성화 (API 서버는 보통 끔)
//                .csrf(AbstractHttpConfigurer::disable)
//
//                // 2. 요청 주소별 권한 설정
//                .authorizeHttpRequests(auth -> auth
//                        // 회원가입, 로그인은 누구나 접속 가능하게 허용
//                        .requestMatchers("/api/users/signup", "/api/users/login").permitAll()
//
//                        // 그 외 모든 요청은 인증(로그인) 필요
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 필터 주입

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 사용 안 함 (JWT는 Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/signup", "/api/users/login").permitAll()
                        // STT 요청은 인증된 사용자만!
                        .requestMatchers("/api/v1/stt/**").authenticated()
                        .anyRequest().authenticated()
                )
                // 필터 추가 (ID/PW 검사 전에 JWT 검사 먼저 하기)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}



//package com.edu.edu_note.global.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (테스트용)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/**").permitAll() // 모든 요청 허용
//                );
//
//        return http.build();
//    }
//}