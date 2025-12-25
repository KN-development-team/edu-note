//package com.edu.edu_note.domain.summary.service;
//
//import com.edu.edu_note.domain.stt.entity.VoiceRecord;
//import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
//import com.edu.edu_note.domain.summary.dto.SummaryResponse;
//import com.edu.edu_note.domain.summary.entity.Summary;
//import com.edu.edu_note.domain.summary.repository.SummaryRepository;
//import com.edu.edu_note.global.exception.BusinessException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.parameters.P;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import com.edu.edu_note.global.exception.ErrorCode;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.Map;
//
//@Service // 의존성 주입
//@RequiredArgsConstructor // final필드만 받는 생성자 자동 생성
//@Transactional(readOnly = true) // 읽기 전용( -> 조회 성능 최적화, 엔티티 변경 방지)
//public class SummaryService {
//
//    private final VoiceRecordRepository voiceRecordRepository;
//    private final SummaryRepository summaryRepository;
//    private final WebClient webClient;
//
//    @Transactional
//    public SummaryResponse createSummary(Long recordId, Long userId) {
//        // record 존재 여부 확인
//        VoiceRecord record = voiceRecordRepository.findById(recordId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));
//
//        // 권한 체크(해당 record가 로그인한 사용자 것인지)
//        if(record.getUser() == null || !record.getUser().getId().equals(userId)){
//            throw new BusinessException(ErrorCode.FORBIDDEN);
//        }
//
//        String content = record.getContent();
//
//        // AI 서버 호출
//        String summaryText = requestSummaryToAi(recordId, content);
//
//        // Summary 저장
//        Summary summary = Summary.builder()
//                .record(record)
//                .content(summaryText)
//                .build();
//        Summary saved = summaryRepository.save(summary);
//
//        // 요약 응답 dto
//        return SummaryResponse.of(
//                recordId,
//                saved.getId(),
//                saved.getContent()
//        );
//    }
//
//    private String requestSummaryToAi(Long recordId, String content) {
//        try {
//            // AI 요청 바디
//            Map<String, Object> requestBody = Map.of(
//                    "record_id", recordId,
//                    "content", content
//            );
//
//            // WebClient로 AI 서버 호출
//            // { message, data: { record_id, summary }, statusCode }
//            Map response = webClient.post()
//                    .uri("/summary")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//
//            // 응답이 null이거나 "data"키가 없으면 예외처리
//            if (response == null || !response.containsKey("data")) {
//                throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
//            }
//
//            Map data = (Map) response.get("data");
//            Object summary = data.get("summary");
//
//            if(summary == null) {
//                throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
//            }
//
//            return summary.toString();
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
//        }
//    }
//
//    // 음성 기록 요약 정보 조회
//    public SummaryResponse summaryInfo(Long recordId) {
//        // 요약 정보 불러오기
//        Summary summary = summaryRepository.findByRecord_Id(recordId)
//                .orElseThrow(() -> {
//                    throw new BusinessException(ErrorCode.SUMMARY_NOT_FOUND);
//                });
//
//        // 불러온 값을 dto 변환 후 반환
//        return SummaryResponse.of(
//                summary.getRecord().getId(),
//                summary.getId(),
//                summary.getContent()
//        );
//    }
//
//}




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
