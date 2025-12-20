package com.edu.edu_note.domain.summary.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // 기본 생성자(Jackson이 객체를 생성하기 때문)
public class SummaryCreateRequest {
    private String content;
}
