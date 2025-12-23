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

    // quizId로 퀴즈 1개 상세 조회 (프론트가 클릭 시 호출하는 API)
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponseDto>> getQuizDetail(
            @PathVariable("quizId") Long quizId
    ) {
        QuizResponseDto dto = quizService.getQuizById(quizId);
        return ResponseEntity.ok(ApiResponse.success("퀴즈 상세 조회 성공", dto));
    }
}

