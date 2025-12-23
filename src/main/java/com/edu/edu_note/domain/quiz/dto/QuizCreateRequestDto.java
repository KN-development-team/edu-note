package com.edu.edu_note.domain.quiz.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizCreateRequestDto {

    // voice_records.id
    private Long recordId;

    // MULTIPLE_CHOICE / OX / SHORT_ANSWER
    private String type;


//    @JsonAlias({"difficulty"})
//    private String level;
    private String difficulty;
}
