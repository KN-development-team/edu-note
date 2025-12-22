package com.edu.edu_note.domain.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SttRecordResponse {
    private Long id;
    private String fileName;   // 화면에 보여줄 파일명
    private Long createdAt;    // epoch millis (날짜 변환)
    private String text;       // 변환 텍스트
}
