package com.edu.edu_note.domain.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuizDetailDto {
    private Long id;
    private Long recordId;
    private String type;
    private String level;
    private String content; // 실제 문제 JSON(배열 문자열)
    private String createdAt;
}
