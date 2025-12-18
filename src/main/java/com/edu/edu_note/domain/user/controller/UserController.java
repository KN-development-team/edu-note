package com.edu.edu_note.domain.user.controller;

import com.edu.edu_note.domain.user.dto.LoginRequest;
import com.edu.edu_note.domain.user.dto.LoginResponse;
import com.edu.edu_note.domain.user.dto.UserSignUpRequest;
import com.edu.edu_note.domain.user.dto.UserSignUpResponse;
import com.edu.edu_note.domain.user.service.UserService;
import com.edu.edu_note.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // user 테이블이므로 주소도 users 권장
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserSignUpResponse>> signUp(@RequestBody UserSignUpRequest request) {
        UserSignUpResponse responseData = userService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED) // HTTP Header Status 201
                .body(ApiResponse.of(
                        201, // Body 내부 statusCode 201
                        "회원가입이 완료되었습니다.",
                        responseData
                ));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("로그인이 완료되었습니다.", response)
        );
    }

}