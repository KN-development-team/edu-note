package com.edu.edu_note.domain.summary.dto;

import com.edu.edu_note.domain.summary.entity.Summary;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder // 서버가 직접 객체를 생성
public class SummaryResponse {
    private Long record_id;
    private Long summary_id;
    private String summary_content;

    public static SummaryResponse of(Long recordId, Long summaryId, String summaryContent){
        return SummaryResponse.builder()
                .record_id(recordId)
                .summary_id(summaryId)
                .summary_content(summaryContent)
                .build();
    }
}
