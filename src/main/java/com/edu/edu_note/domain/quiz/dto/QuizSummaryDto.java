package com.edu.edu_note.domain.quiz.dto;

//퀴즈 기록 화면에 뿌릴 요약 정보만 담는 DTO
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuizSummaryDto {
    private Long id;
    private String type;
    private String level;
    private String createdAt;
}
