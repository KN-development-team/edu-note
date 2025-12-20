//package com.edu.edu_note.domain.stt.controller;
//
//import com.edu.edu_note.domain.stt.service.SttService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//@RestController
//@RequestMapping("/api/v1/stt")
//@RequiredArgsConstructor
//public class SttController {
//    private final SttService sttService;
//
//    @PostMapping
//    public ResponseEntity<String> convertStt(@RequestParam("file") MultipartFile file) {
//        try {
//            String result = sttService.convertVoiceToText(file);
//            return ResponseEntity.ok(result);
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().body("파일 처리 중 오류 발생: " + e.getMessage());
//        }
//    }
//}


package com.edu.edu_note.domain.stt.controller;

import com.edu.edu_note.domain.stt.service.SttService;
import com.edu.edu_note.global.auth.CustomUserDetails;
import com.edu.edu_note.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/stt")
@RequiredArgsConstructor
public class SttController {

    private final SttService sttService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> convertStt(
            // 1. 토큰에서 추출한 사용자 정보 (로그인 안했으면 여기서 걸림)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 2. 서비스 실행 (사용자 정보 + 파일)
            String result = sttService.convertVoiceToText(userDetails.getUser(), file);

            // 3. 성공 응답
            return ResponseEntity.ok(ApiResponse.success("STT 변환 및 저장이 완료되었습니다.", result));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.of(500, "파일 처리 중 오류가 발생했습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.of(500, "알 수 없는 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
}