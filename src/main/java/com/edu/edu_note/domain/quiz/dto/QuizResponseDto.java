package com.edu.edu_note.domain.quiz.dto; //퀴즈 기록 조회용

import com.edu.edu_note.domain.quiz.entity.Quiz;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuizResponseDto {
    private Long id;
    private Long recordId;
    private String type;
    private String level;      // DB 컬럼명 level
    private String content;    // JSON 문자열 그대로 저장한 값
    private LocalDateTime createdAt;

    public static QuizResponseDto from(Quiz quiz) {
        return QuizResponseDto.builder()
                .id(quiz.getId())
                .recordId(quiz.getVoiceRecord().getId())
                .type(quiz.getType())
                .level(quiz.getLevel())
                .content(quiz.getContent())
                .createdAt(quiz.getCreatedAt())
                .build();
    }
}
