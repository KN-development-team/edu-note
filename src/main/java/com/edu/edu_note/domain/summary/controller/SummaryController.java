package com.edu.edu_note.domain.summary.controller;

import com.edu.edu_note.domain.summary.dto.SummaryResponse;
import com.edu.edu_note.domain.summary.service.SummaryService;
import com.edu.edu_note.global.auth.CustomUserDetails;
import com.edu.edu_note.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class SummaryController {

    private final SummaryService summaryService;

    // 요약 생성: POST /api/records/{recordId}/summary
    @PostMapping("/{recordId}/summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> createSummary(
            @PathVariable("recordId") Long recordId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        SummaryResponse summaryResponse = summaryService.createSummary(recordId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("AI 요약이 완료되었습니다.", summaryResponse)
        );
    }

    // 요약 조회: GET /api/records/{recordId}/summary
    @GetMapping("/{recordId}/summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> summaryInfo(
            @PathVariable("recordId") Long recordId
    ) {
        SummaryResponse summaryResponse = summaryService.summaryInfo(recordId);

        return ResponseEntity.ok(
                ApiResponse.success("음성 기록의 요약 정보가 조회되었습니다.", summaryResponse)
        );
    }
}
