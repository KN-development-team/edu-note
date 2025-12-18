package com.edu.edu_note.domain.user.service;

import com.edu.edu_note.domain.user.dto.LoginRequest;
import com.edu.edu_note.domain.user.dto.LoginResponse;
import com.edu.edu_note.domain.user.dto.UserSignUpRequest;
import com.edu.edu_note.domain.user.dto.UserSignUpResponse;
import com.edu.edu_note.domain.user.entity.User;
import com.edu.edu_note.domain.user.repository.UserRepository;
import com.edu.edu_note.global.exception.BusinessException;
import com.edu.edu_note.global.exception.ErrorCode;
import com.edu.edu_note.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserSignUpResponse signUp(UserSignUpRequest request) {
        // 1. 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 암호화 (추후 적용, 현재는 그대로)
        String encodedPassword = request.getPassword();

        // 3. 저장
        User savedUser = userRepository.save(request.toEntity(encodedPassword));

        // 4. 응답 DTO로 변환해서 반환
        return UserSignUpResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 회원 조회 (없으면 로그인 실패 예외)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        // 2. 비밀번호 일치 확인 (일단 문자열 비교, 추후 암호화 적용 시 변경 필요)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 3. 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        // 4. 응답 DTO 만들기
        return LoginResponse.of(user, accessToken, refreshToken);
    }
}