//package com.edu.edu_note.domain.quiz.service;
//
//import com.edu.edu_note.domain.quiz.dto.QuizCreateRequestDto;
//import com.edu.edu_note.domain.quiz.dto.QuizResponseDto;
//import com.edu.edu_note.domain.quiz.entity.Quiz;
//import com.edu.edu_note.domain.quiz.repository.QuizRepository;
//import com.edu.edu_note.domain.stt.entity.VoiceRecord;
//import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
//import com.edu.edu_note.global.exception.BusinessException;
//import com.edu.edu_note.global.exception.ErrorCode;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class QuizService {
//
//    private final VoiceRecordRepository voiceRecordRepository;
//    private final QuizRepository quizRepository;
//    private final WebClient.Builder webClientBuilder;
//
//    // 응답 파싱용
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Value("${ai.server.url}")
//    private String aiServerUrl;
//
//    @Transactional
//    public QuizResponseDto createQuiz(QuizCreateRequestDto requestDto) {
//
//        VoiceRecord record = voiceRecordRepository.findById(requestDto.getRecordId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
//
//        // 1) DB 저장용 level 정규화
//        String levelForDb = normalizeLevelForDb(requestDto.getDifficulty());
//
//        // 2) AI 서버로 보낼 difficulty 정규화
//        String difficultyForAi = normalizeDifficultyForAi(requestDto.getDifficulty());
//
//        String type = (requestDto.getType() == null)
//                ? "MULTIPLE_CHOICE"
//                : requestDto.getType().trim().toUpperCase();
//
//        Map<String, String> aiRequest = new HashMap<>();
//        aiRequest.put("text", record.getContent());
//        aiRequest.put("type", type);
//        aiRequest.put("difficulty", difficultyForAi);
//
//        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();
//
//        String responseJson = webClient.post()
//                .uri("/quiz")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(aiRequest)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        System.out.println("AI RESPONSE = " + responseJson);
//
//
//        // DB에는 배열(JSON)만 저장하도록 정리
//        String normalizedContent = normalizeAiQuizContent(responseJson);
//
//        try {
//            Quiz quiz = Quiz.builder()
//                    .voiceRecord(record)
//                    .type(type)
//                    .level(levelForDb)
//                    .content(normalizedContent)
//                    .build();
//
//            Quiz saved = quizRepository.save(quiz);
//            return QuizResponseDto.from(saved);
//
//        } catch (DataIntegrityViolationException e) {
//            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public List<QuizResponseDto> getQuizzesByRecordId(Long recordId) {
//        return quizRepository.findAllByVoiceRecord_IdOrderByCreatedAtDesc(recordId)
//                .stream()
//                .map(QuizResponseDto::from)
//                .toList();
//    }
//
//    @Transactional(readOnly = true)
//    public QuizResponseDto getQuizById(Long quizId) {
//        Quiz quiz = quizRepository.findById(quizId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
//        return QuizResponseDto.from(quiz);
//    }
//
//    /**
//     * AI 응답을 프론트가 바로 JSON.parse 가능한 배열 문자열로 정규화
//     *
//     * - AI가 {"quiz":"[ ... ]"} 형태로 주면 -> quiz 값만 꺼내서 저장
//     * - AI가 {"quiz":[ ... ]} 형태로 주면 -> 배열을 문자열로 변환해서 저장
//     * - 이미 [ ... ] 배열 문자열이면 -> 그대로 저장
//     * - 파싱 실패하면 -> 원본 저장 (최소 보존)
//     */
//    private String normalizeAiQuizContent(String responseJson) {
//        if (responseJson == null) return "[]";
//
//        String trimmed = responseJson.trim();
//        // 이미 배열이면 그대로 저장
//        if (trimmed.startsWith("[")) return trimmed;
//
//        try {
//            JsonNode root = objectMapper.readTree(trimmed);
//
//            JsonNode quizNode = root.get("quiz");
//            if (quizNode == null || quizNode.isNull()) {
//                // quiz 키가 없으면 원본 그대로(혹은 []로 강제하고 싶으면 여기서 바꾸면 됨)
//                return trimmed;
//            }
//
//            // quiz가 문자열이면: 그 문자열 자체가 "[{...}]" 형태라서 그대로 리턴
//            if (quizNode.isTextual()) {
//                String quizText = quizNode.asText();
//                return (quizText == null || quizText.isBlank()) ? "[]" : quizText;
//            }
//
//            // quiz가 배열/객체면: 문자열로 변환해서 저장
//            return objectMapper.writeValueAsString(quizNode);
//
//        } catch (Exception e) {
//            // 파싱 실패 시 원본 보존
//            return trimmed;
//        }
//    }
//
//    private String normalizeLevelForDb(String input) {
//        if (input == null) return "NORMAL";
//        String v = input.trim().toUpperCase();
//
//        return switch (v) {
//            case "MEDIUM" -> "NORMAL";
//            case "HIGH" -> "HARD";
//            case "LOW" -> "EASY";
//            case "EASY", "NORMAL", "HARD" -> v;
//            default -> "NORMAL";
//        };
//    }
//
//    private String normalizeDifficultyForAi(String input) {
//        if (input == null) return "MEDIUM";
//        String v = input.trim().toUpperCase();
//
//        return switch (v) {
//            case "NORMAL" -> "MEDIUM";
//            case "HIGH" -> "HARD";
//            case "LOW" -> "EASY";
//            case "EASY", "MEDIUM", "HARD" -> v;
//            default -> "MEDIUM";
//        };
//    }
//}
//
//


package com.edu.edu_note.domain.quiz.service;

import com.edu.edu_note.domain.quiz.dto.QuizCreateRequestDto;
import com.edu.edu_note.domain.quiz.dto.QuizResponseDto;
import com.edu.edu_note.domain.quiz.entity.Quiz;
import com.edu.edu_note.domain.quiz.repository.QuizRepository;
import com.edu.edu_note.domain.stt.entity.VoiceRecord;
import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
import com.edu.edu_note.global.exception.BusinessException;
import com.edu.edu_note.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final VoiceRecordRepository voiceRecordRepository;
    private final QuizRepository quizRepository;
    private final WebClient.Builder webClientBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Transactional
    public QuizResponseDto createQuiz(QuizCreateRequestDto requestDto) {

        VoiceRecord record = voiceRecordRepository.findById(requestDto.getRecordId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String levelForDb = normalizeLevelForDb(requestDto.getDifficulty());
        String difficultyForAi = normalizeDifficultyForAi(requestDto.getDifficulty());
        String type = normalizeType(requestDto.getType()); // ✅ 타입 정규화

        Map<String, String> aiRequest = new HashMap<>();
        aiRequest.put("text", record.getContent());
        aiRequest.put("type", type);
        aiRequest.put("difficulty", difficultyForAi);

        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();

        String responseJson = webClient.post()
                .uri("/quiz")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(aiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 디버깅용(원하면 제거)
        System.out.println("AI RESPONSE = " + responseJson);

        // ✅ content를 프론트가 바로 JSON.parse 가능한 "배열 문자열"로 정규화
        String normalizedContent = normalizeAiQuizContent(responseJson);

        try {
            Quiz quiz = Quiz.builder()
                    .voiceRecord(record)
                    .type(type)
                    .level(levelForDb)
                    .content(normalizedContent)
                    .build();

            Quiz saved = quizRepository.save(quiz);
            return QuizResponseDto.from(saved);

        } catch (DataIntegrityViolationException e) {
            // 여기서 너는 OX가 DB 제약에 걸려서 터졌던 것
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Transactional(readOnly = true)
    public List<QuizResponseDto> getQuizzesByRecordId(Long recordId) {
        return quizRepository.findAllByVoiceRecord_IdOrderByCreatedAtDesc(recordId)
                .stream()
                .map(QuizResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizResponseDto getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        return QuizResponseDto.from(quiz);
    }

    // ✅ AI 응답 정규화: {"quiz":"[...]" } 또는 {"quiz":[...]} 또는 "[...]" 모두 -> "[...]" 로 저장
    private String normalizeAiQuizContent(String responseJson) {
        if (responseJson == null) return "[]";
        String trimmed = responseJson.trim();

        if (trimmed.startsWith("[")) return trimmed;

        try {
            JsonNode root = objectMapper.readTree(trimmed);
            JsonNode quizNode = root.get("quiz");

            if (quizNode == null || quizNode.isNull()) return trimmed;

            if (quizNode.isTextual()) {
                String quizText = quizNode.asText();
                return (quizText == null || quizText.isBlank()) ? "[]" : quizText;
            }

            return objectMapper.writeValueAsString(quizNode);

        } catch (Exception e) {
            return trimmed;
        }
    }

    // ✅ 타입을 DB/AI가 모두 이해할 수 있는 값으로 고정
    private String normalizeType(String input) {
        if (input == null) return "MULTIPLE_CHOICE";
        String v = input.trim().toUpperCase();

        return switch (v) {
            case "MC", "MULTIPLE", "MULTIPLE_CHOICE" -> "MULTIPLE_CHOICE";
            case "SA", "SHORT", "SHORT_ANSWER" -> "SHORT_ANSWER";
            case "OX", "O/X", "TRUE_FALSE", "TF" -> "OX";
            default -> "MULTIPLE_CHOICE";
        };
    }

    private String normalizeLevelForDb(String input) {
        if (input == null) return "NORMAL";
        String v = input.trim().toUpperCase();

        return switch (v) {
            case "MEDIUM" -> "NORMAL";
            case "HIGH" -> "HARD";
            case "LOW" -> "EASY";
            case "EASY", "NORMAL", "HARD" -> v;
            default -> "NORMAL";
        };
    }

    private String normalizeDifficultyForAi(String input) {
        if (input == null) return "MEDIUM";
        String v = input.trim().toUpperCase();

        return switch (v) {
            case "NORMAL" -> "MEDIUM";
            case "HIGH" -> "HARD";
            case "LOW" -> "EASY";
            case "EASY", "MEDIUM", "HARD" -> v;
            default -> "MEDIUM";
        };
    }
}
