package com.edu.edu_note.domain.quiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizRequestDto {
    private Long recordId; // voice_records.id
    private String type;   // MULTIPLE_CHOICE, OX, SHORT_ANSWER
    private String level;  // EASY, MEDIUM, HARD (DB: level)
}
