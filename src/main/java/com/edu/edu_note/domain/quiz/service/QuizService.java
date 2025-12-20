package com.edu.edu_note.domain.quiz.service;

import com.edu.edu_note.domain.quiz.dto.QuizRequestDto;
import com.edu.edu_note.domain.quiz.entity.Quiz;
import com.edu.edu_note.domain.quiz.repository.QuizRepository;
import com.edu.edu_note.domain.stt.entity.VoiceRecord;
import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
import com.edu.edu_note.global.exception.BusinessException;
import com.edu.edu_note.global.exception.ErrorCode;
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

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Transactional
    public String createQuiz(QuizRequestDto requestDto) {
        // 1. 녹음본(VoiceRecord) 찾기
        VoiceRecord record = voiceRecordRepository.findById(requestDto.getNoteId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. AI 서버 요청 데이터 준비
        Map<String, String> aiRequest = new HashMap<>();
        aiRequest.put("text", record.getContent());
        aiRequest.put("type", requestDto.getType());
        aiRequest.put("difficulty", requestDto.getDifficulty());

        // 3. AI 서버 호출
        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();
        String responseJson = webClient.post()
                .uri("/quiz")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(aiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 4. 퀴즈 DB 저장
        Quiz quiz = Quiz.builder()
                .voiceRecord(record)
                .content(responseJson)
                .build();
        quizRepository.save(quiz);

        return responseJson;
    }
}