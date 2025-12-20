package com.edu.edu_note.domain.quiz.controller;

import com.edu.edu_note.domain.quiz.dto.QuizRequestDto;
import com.edu.edu_note.domain.quiz.service.QuizService;
import com.edu.edu_note.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createQuiz(@RequestBody QuizRequestDto requestDto) {
        String result = quizService.createQuiz(requestDto);
        return ResponseEntity.ok(ApiResponse.success("퀴즈가 생성되었습니다.", result));
    }
}
