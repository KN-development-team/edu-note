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
        String type = normalizeType(requestDto.getType()); // 타입 정규화

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

        // content를 프론트가 바로 JSON.parse 가능한 "배열 문자열"로 정규화
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

    // AI 응답 정규화: {"quiz":"[...]" } 또는 {"quiz":[...]} 또는 "[...]" 모두 -> "[...]" 로 저장
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

    // 타입을 DB/AI가 모두 이해할 수 있는 값으로 고정
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
