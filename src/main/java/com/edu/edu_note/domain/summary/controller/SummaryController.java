package com.edu.edu_note.domain.summary.controller;

import com.edu.edu_note.domain.summary.dto.SummaryCreateRequest;
import com.edu.edu_note.domain.summary.dto.SummaryResponse;
import com.edu.edu_note.domain.summary.service.SummaryService;
import com.edu.edu_note.global.auth.CustomUserDetails;
import com.edu.edu_note.global.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<SummaryResponse>> craeteSummary(
            @PathVariable Long recordId,
            @RequestBody SummaryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
                Long userId = userDetails.getUser().getId();
                SummaryResponse summaryResponse = summaryService.createSummary(recordId, userId);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.of(
                                201,
                                "AI 요약이 완료되었습니다.",
                                summaryResponse
                        ));
    }
}
