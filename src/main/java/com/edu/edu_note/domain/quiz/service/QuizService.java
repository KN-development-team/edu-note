////package com.edu.edu_note.domain.quiz.service;
////
////import com.edu.edu_note.domain.quiz.dto.QuizRequestDto;
////import com.edu.edu_note.domain.quiz.entity.Quiz;
////import com.edu.edu_note.domain.quiz.repository.QuizRepository;
////import com.edu.edu_note.domain.stt.entity.VoiceRecord;
////import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
////import com.edu.edu_note.global.exception.BusinessException;
////import com.edu.edu_note.global.exception.ErrorCode;
////import lombok.RequiredArgsConstructor;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.http.MediaType;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////import org.springframework.web.reactive.function.client.WebClient;
////
////import java.util.HashMap;
////import java.util.Map;
////
////@Service
////@RequiredArgsConstructor
////public class QuizService {
////
////    private final VoiceRecordRepository voiceRecordRepository;
////    private final QuizRepository quizRepository;
////    private final WebClient.Builder webClientBuilder;
////
////    @Value("${ai.server.url}")
////    private String aiServerUrl;
////
////    @Transactional
////    public String createQuiz(QuizRequestDto requestDto) {
////        // 1. 녹음본(VoiceRecord) 찾기
////        VoiceRecord record = voiceRecordRepository.findById(requestDto.getNoteId())
////                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
////
////        // 2. AI 서버 요청 데이터 준비
////        Map<String, String> aiRequest = new HashMap<>();
////        aiRequest.put("text", record.getContent());
////        aiRequest.put("type", requestDto.getType());
////        aiRequest.put("difficulty", requestDto.getDifficulty());
////
////        // 3. AI 서버 호출
////        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();
////        String responseJson = webClient.post()
////                .uri("/quiz")
////                .contentType(MediaType.APPLICATION_JSON)
////                .bodyValue(aiRequest)
////                .retrieve()
////                .bodyToMono(String.class)
////                .block();
////
////        // 4. 퀴즈 DB 저장
////        Quiz quiz = Quiz.builder()
////                .voiceRecord(record)
////                .content(responseJson)
////                .build();
////        quizRepository.save(quiz);
////
////        return responseJson;
////    }
////}
//
//
//package com.edu.edu_note.domain.quiz.service;
//
//import com.edu.edu_note.domain.quiz.dto.QuizRequestDto;
//import com.edu.edu_note.domain.quiz.entity.Quiz;
//import com.edu.edu_note.domain.quiz.repository.QuizRepository;
//import com.edu.edu_note.domain.stt.entity.VoiceRecord;
//import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
//import com.edu.edu_note.global.exception.BusinessException;
//import com.edu.edu_note.global.exception.ErrorCode;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.HashMap;
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
//    @Value("${ai.server.url}")
//    private String aiServerUrl;
//
//    @Transactional
//    public String createQuiz(QuizRequestDto requestDto) {
//        // 1. 녹음본(VoiceRecord) 찾기
//        VoiceRecord record = voiceRecordRepository.findById(requestDto.getNoteId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
//
//        // 2. AI 서버 요청 데이터 준비
//        Map<String, String> aiRequest = new HashMap<>();
//        aiRequest.put("text", record.getContent());
//        aiRequest.put("type", requestDto.getType());
//        aiRequest.put("difficulty", requestDto.getDifficulty());
//
//        // 3. AI 서버 호출
//        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();
//        String responseJson = webClient.post()
//                .uri("/quiz")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(aiRequest)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        // 4. 퀴즈 DB 저장
//        // 주의: Quiz 엔티티에 type과 difficulty 필드가 추가되어 있어야 빨간 줄이 안 뜹니다.
//        Quiz quiz = Quiz.builder()
//                .voiceRecord(record)
//                .type(requestDto.getType())             // [수정됨]
//                .difficulty(requestDto.getDifficulty()) // [수정됨]
//                .content(responseJson)
//                .build();
//
//        quizRepository.save(quiz);
//
//        return responseJson;
//    }
//}



package com.edu.edu_note.domain.quiz.service;

import com.edu.edu_note.domain.quiz.dto.QuizRequestDto;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final VoiceRecordRepository voiceRecordRepository;
    private final QuizRepository quizRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Transactional
    public QuizResponseDto createQuiz(QuizRequestDto requestDto) {

        // 1) record 조회
        VoiceRecord record = voiceRecordRepository.findById(requestDto.getRecordId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2) AI 요청 바디
        Map<String, String> aiRequest = new HashMap<>();
        aiRequest.put("text", record.getContent());
        aiRequest.put("type", requestDto.getType());
        aiRequest.put("difficulty", requestDto.getDifficulty());

        // 3) AI 서버 호출 (/quiz)
        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();

        String responseJson = webClient.post()
                .uri("/quiz")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(aiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (responseJson == null || responseJson.isBlank()) {
            throw new RuntimeException("AI 서버 응답이 비어있습니다.");
        }

        // {"quiz": "..."} 에서 quiz만 추출
        String quizContent = extractQuizContent(responseJson);

        // 4) DB 저장 (DB 컬럼: level)
        Quiz quiz = Quiz.builder()
                .voiceRecord(record)
                .type(requestDto.getType())
                .difficulty(requestDto.getDifficulty()) // -> @Column(name="level")
                .content(quizContent)
                .build();

        Quiz saved = quizRepository.save(quiz);

        // 5) 응답 (프론트가 보기 쉽게 level로 내려줌)
        return new QuizResponseDto(
                saved.getId(),
                record.getId(),
                saved.getType(),
                saved.getDifficulty(), // level 값
                saved.getContent()
        );
    }

    private String extractQuizContent(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode quizNode = root.get("quiz");
            if (quizNode != null && !quizNode.isNull()) {
                return quizNode.asText();
            }
            return responseJson;
        } catch (Exception e) {
            return responseJson;
        }
    }
}
