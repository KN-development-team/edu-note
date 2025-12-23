package com.edu.edu_note.domain.quiz.dto;

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
