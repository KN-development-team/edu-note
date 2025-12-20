package com.edu.edu_note.domain.quiz.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizRequestDto {
    private Long noteId;      // 어떤 STT 노트로 만들 것인지
    private String type;      // 객관식, 주관식 등 (MULTIPLE_CHOICE, SHORT_ANSWER)
    private String difficulty;// 상, 중, 하 (HIGH, MEDIUM, LOW)
}
