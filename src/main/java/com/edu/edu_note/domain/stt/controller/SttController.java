package com.edu.edu_note.domain.stt.controller;

import com.edu.edu_note.domain.stt.dto.SttRecordResponse;
import com.edu.edu_note.domain.stt.dto.SttResponse;
import com.edu.edu_note.domain.stt.service.SttService;
import com.edu.edu_note.global.auth.CustomUserDetails;
import com.edu.edu_note.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stt")
@RequiredArgsConstructor
public class SttController {

    private final SttService sttService;

    @PostMapping
    public ResponseEntity<ApiResponse<SttResponse>> convertStt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            SttResponse result = sttService.convertVoiceToText(userDetails.getUser(), file);
            return ResponseEntity.ok(ApiResponse.success("STT 변환 및 저장이 완료되었습니다.", result));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.of(500, "파일 처리 중 오류가 발생했습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.of(500, "알 수 없는 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // 새로고침해도 기록 유지: DB에서 내 기록 목록 조회
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<SttRecordResponse>>> getMyRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<SttRecordResponse> records = sttService.getMyRecords(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success("내 학습 기록 조회 성공", records));
    }
}
