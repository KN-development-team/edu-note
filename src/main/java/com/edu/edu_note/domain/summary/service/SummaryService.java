package com.edu.edu_note.domain.summary.service;

import com.edu.edu_note.domain.stt.entity.VoiceRecord;
import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
import com.edu.edu_note.domain.summary.dto.SummaryResponse;
import com.edu.edu_note.domain.summary.entity.Summary;
import com.edu.edu_note.domain.summary.repository.SummaryRepository;
import com.edu.edu_note.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.edu.edu_note.global.exception.ErrorCode;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service // 의존성 주입
@RequiredArgsConstructor // final필드만 받는 생성자 자동 생성
@Transactional(readOnly = true) // 읽기 전용( -> 조회 성능 최적화, 엔티티 변경 방지)
public class SummaryService {

    private final VoiceRecordRepository voiceRecordRepository;
    private final SummaryRepository summaryRepository;
    private final WebClient webClient;

    @Transactional
    public SummaryResponse createSummary(Long recordId, Long userId) {
        // record 존재 여부 확인
        VoiceRecord record = voiceRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));

        // 권한 체크(해당 record가 로그인한 사용자 것인지)
        if(record.getUser() == null || !record.getUser().getId().equals(userId)){
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String content = record.getContent();

        // AI 서버 호출
        String summaryText = requestSummaryToAi(recordId, content);

        // Summary 저장
        Summary summary = Summary.builder()
                .record(record)
                .content(summaryText)
                .build();
        Summary saved = summaryRepository.save(summary);

        // 요약 응답 dto
        return SummaryResponse.of(
                recordId,
                saved.getId(),
                saved.getContent()
        );
    }

    private String requestSummaryToAi(Long recordId, String content) {
        try {
            // AI 요청 바디
            Map<String, Object> requestBody = Map.of(
                    "record_id", recordId,
                    "content", content
            );

            // WebClient로 AI 서버 호출
            // { message, data: { record_id, summary }, statusCode }
            Map response = webClient.post()
                    .uri("/summary")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 응답이 null이거나 "data"키가 없으면 예외처리
            if (response == null || !response.containsKey("data")) {
                throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
            }

            Map data = (Map) response.get("data");
            Object summary = data.get("summary");

            if(summary == null) {
                throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
            }

            return summary.toString();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
        }
    }
}
