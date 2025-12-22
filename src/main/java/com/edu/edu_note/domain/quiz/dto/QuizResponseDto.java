package com.edu.edu_note.domain.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuizResponseDto {
    private Long id;         // quiz.id
    private Long recordId;   // quiz.record_id
    private String type;     // quiz.type
    private String level;    // quiz.level
    private String content;  // quiz.content (AI가 만든 결과)
}
