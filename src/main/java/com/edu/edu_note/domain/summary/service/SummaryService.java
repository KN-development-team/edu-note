package com.edu.edu_note.domain.summary.service;

import com.edu.edu_note.domain.stt.entity.VoiceRecord;
import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
import com.edu.edu_note.domain.summary.dto.SummaryResponse;
import com.edu.edu_note.domain.summary.entity.Summary;
import com.edu.edu_note.domain.summary.repository.SummaryRepository;
import com.edu.edu_note.global.exception.BusinessException;
import com.edu.edu_note.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SummaryService {

    private final VoiceRecordRepository voiceRecordRepository;
    private final SummaryRepository summaryRepository;
    private final WebClient.Builder webClientBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Transactional
    public SummaryResponse createSummary(Long recordId, Long userId) {

        VoiceRecord record = voiceRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));

        if (record.getUser() == null || !record.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String content = record.getContent();

        // AI 서버 호출 (응답 파싱 강화)
        String summaryText = requestSummaryToAi(recordId, content);

        // 이미 요약이 있으면 UPDATE, 없으면 INSERT
        Summary summary = summaryRepository.findByRecord_Id(recordId).orElse(null);

        if (summary == null) {
            summary = Summary.builder()
                    .record(record)
                    .content(summaryText)
                    .build();
        } else {
            summary.updateContent(summaryText);
        }

        Summary saved = summaryRepository.save(summary);

        return SummaryResponse.of(
                recordId,
                saved.getId(),
                saved.getContent()
        );
    }

    private String requestSummaryToAi(Long recordId, String content) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "record_id", recordId,
                    "content", content
            );

            // baseUrl을 잡고 호출
            String raw = webClientBuilder.baseUrl(aiServerUrl).build()
                    .post()
                    .uri("/summary")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (raw == null || raw.isBlank()) {
                throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
            }

            // AI가 JSON이 아닌 plain text로 줄 수도 있으니 처리
            String t = raw.trim();
            if (!t.startsWith("{") && !t.startsWith("[")) {
                return raw;
            }

            JsonNode root = objectMapper.readTree(t);

            // 가능한 응답 형태들을 모두 수용
            // 1) { data: { summary: "..." } }
            // 2) { data: { summary_content: "..." } }
            // 3) { summary: "..." }
            // 4) { summary_content: "..." }
            JsonNode dataNode = root.has("data") ? root.get("data") : null;

            JsonNode summaryNode = null;
            if (dataNode != null) {
                if (dataNode.has("summary")) summaryNode = dataNode.get("summary");
                else if (dataNode.has("summary_content")) summaryNode = dataNode.get("summary_content");
            }
            if (summaryNode == null) {
                if (root.has("summary")) summaryNode = root.get("summary");
                else if (root.has("summary_content")) summaryNode = root.get("summary_content");
            }

            if (summaryNode == null || summaryNode.isNull()) {
                throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
            }

            return summaryNode.isTextual() ? summaryNode.asText() : summaryNode.toString();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
        }
    }

    public SummaryResponse summaryInfo(Long recordId) {
        Summary summary = summaryRepository.findByRecord_Id(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUMMARY_NOT_FOUND));

        return SummaryResponse.of(
                summary.getRecord().getId(),
                summary.getId(),
                summary.getContent()
        );
    }
}
