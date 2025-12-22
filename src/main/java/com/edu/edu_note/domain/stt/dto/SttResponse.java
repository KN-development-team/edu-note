package com.edu.edu_note.domain.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SttResponse {
    private Long id;     // 프론트엔드가 퀴즈 만들 때 쓸 ID
    private String text; // 화면에 보여줄 변환된 텍스트
}
