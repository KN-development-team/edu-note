package com.edu.edu_note.domain.summary.controller;

import com.edu.edu_note.domain.summary.dto.SummaryResponse;
import com.edu.edu_note.domain.summary.service.SummaryService;
import com.edu.edu_note.global.auth.CustomUserDetails;
import com.edu.edu_note.global.response.ApiResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class SummaryController {
    private final SummaryService summaryService;
    /**
     * 요약 생성
     * POST /api/records/{recordId}/summary
     */

    @PostMapping("/{recordId}/summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> createSummary(
            @PathVariable Long recordId,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
                Long userId = userDetails.getUser().getId();
                SummaryResponse summaryResponse = summaryService.createSummary(recordId, userId); // 응답 dto summaryResponse에 반환값 매핑

                return ResponseEntity.ok(
                        ApiResponse.success(
                                "AI 요약이 완료되었습니다.",
                                summaryResponse
                        ));
    }

    // 요약 정보 조회
    @GetMapping("/{recordId}/summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> summaryInfo(
            @PathVariable Long recordId
    ) {
        SummaryResponse summaryResponse = summaryService.summaryInfo(recordId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "음성 기록의 요약 정보가 조회되었습니다.",
                        summaryResponse
                ));
    }

}
