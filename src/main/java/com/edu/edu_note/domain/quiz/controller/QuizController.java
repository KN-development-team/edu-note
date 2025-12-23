//package com.edu.edu_note.domain.quiz.controller;
//
//import com.edu.edu_note.domain.quiz.dto.QuizRequestDto;
//import com.edu.edu_note.domain.quiz.dto.QuizResponseDto;
//import com.edu.edu_note.domain.quiz.service.QuizService;
//import com.edu.edu_note.global.response.ApiResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/quiz")
//@RequiredArgsConstructor
//public class QuizController {
//
//    private final QuizService quizService;
//
//    // URL: /api/v1/quiz
//    @PostMapping
//    public ResponseEntity<ApiResponse<QuizResponseDto>> createQuiz(@RequestBody QuizRequestDto requestDto) {
//        QuizResponseDto result = quizService.createQuiz(requestDto);
//        return ResponseEntity.ok(ApiResponse.success("퀴즈가 생성되었습니다.", result));
//    }
//}


//package com.edu.edu_note.domain.quiz.controller;
//
//import com.edu.edu_note.domain.quiz.dto.QuizCreateRequestDto;
//import com.edu.edu_note.domain.quiz.dto.QuizCreateResponseDto;
//import com.edu.edu_note.domain.quiz.dto.QuizDetailDto;
//import com.edu.edu_note.domain.quiz.dto.QuizSummaryDto;
//import com.edu.edu_note.domain.quiz.service.QuizService;
//import com.edu.edu_note.global.auth.CustomUserDetails;
//import com.edu.edu_note.global.response.ApiResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/quiz")
//@RequiredArgsConstructor
//public class QuizController {
//
//    private final QuizService quizService;
//
//    // 1) 퀴즈 생성
//    @PostMapping
//    public ResponseEntity<ApiResponse<QuizCreateResponseDto>> createQuiz(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @RequestBody QuizCreateRequestDto requestDto
//    ) {
//        QuizCreateResponseDto result = quizService.createQuiz(userDetails.getUser(), requestDto);
//        return ResponseEntity.ok(ApiResponse.success("퀴즈가 생성되었습니다.", result));
//    }
//
//    // 2) 특정 녹음(recordId)의 퀴즈 기록 목록 조회 (최신순)
//    @GetMapping("/records/{recordId}")
//    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getQuizRecords(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @PathVariable Long recordId
//    ) {
//        List<QuizSummaryDto> list = quizService.getQuizRecords(userDetails.getUser(), recordId);
//        return ResponseEntity.ok(ApiResponse.success("퀴즈 기록을 불러왔습니다.", list));
//    }
//
//    // 3) 퀴즈 상세 조회 (실제 문제 JSON 포함)
//    @GetMapping("/{quizId}")
//    public ResponseEntity<ApiResponse<QuizDetailDto>> getQuizDetail(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @PathVariable Long quizId
//    ) {
//        QuizDetailDto detail = quizService.getQuizDetail(userDetails.getUser(), quizId);
//        return ResponseEntity.ok(ApiResponse.success("퀴즈를 불러왔습니다.", detail));
//    }
//}


package com.edu.edu_note.domain.quiz.controller;

import com.edu.edu_note.domain.quiz.dto.QuizCreateRequestDto;
import com.edu.edu_note.domain.quiz.dto.QuizResponseDto;
import com.edu.edu_note.domain.quiz.service.QuizService;
import com.edu.edu_note.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponseDto>> createQuiz(@RequestBody QuizCreateRequestDto requestDto) {
        QuizResponseDto result = quizService.createQuiz(requestDto);
        return ResponseEntity.ok(ApiResponse.success("퀴즈가 생성되었습니다.", result));
    }

    // recordId로 퀴즈 목록 조회
    @GetMapping("/records/{recordId}")
    public ResponseEntity<ApiResponse<List<QuizResponseDto>>> getQuizRecords(
            @PathVariable("recordId") Long recordId
    ) {
        List<QuizResponseDto> list = quizService.getQuizzesByRecordId(recordId);
        return ResponseEntity.ok(ApiResponse.success("퀴즈 기록 조회 성공", list));
    }

    // ✅ quizId로 퀴즈 1개 상세 조회 (프론트가 클릭 시 호출하는 API)
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponseDto>> getQuizDetail(
            @PathVariable("quizId") Long quizId
    ) {
        QuizResponseDto dto = quizService.getQuizById(quizId);
        return ResponseEntity.ok(ApiResponse.success("퀴즈 상세 조회 성공", dto));
    }
}

