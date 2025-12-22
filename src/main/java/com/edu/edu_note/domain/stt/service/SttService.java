package com.edu.edu_note.domain.stt.service;

import com.edu.edu_note.domain.stt.dto.SttRecordResponse;
import com.edu.edu_note.domain.stt.dto.SttResponse;
import com.edu.edu_note.domain.stt.entity.VoiceRecord;
import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
import com.edu.edu_note.domain.user.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttService {

    private final S3Template s3Template;
    private final WebClient.Builder webClientBuilder;
    private final VoiceRecordRepository voiceRecordRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public SttResponse convertVoiceToText(User user, MultipartFile file) throws IOException {

        // 1. S3 업로드
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String s3Url = s3Template.upload(bucketName, fileName, file.getInputStream()).getURL().toString();
        log.info("S3 Upload Success: {}", s3Url);

        // 2. AI 서버 요청
        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        String rawJson = webClient.post()
                .uri("/stt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // JSON에서 text만 추출
        String pureText;
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            pureText = root.path("text").asText("");
        } catch (Exception e) {
            log.error("JSON 파싱 실패", e);
            pureText = rawJson;
        }

        // 3. DB 저장
        VoiceRecord record = VoiceRecord.builder()
                .user(user)
                .audioS3Key(s3Url)
                .content(pureText)
                .build();

        VoiceRecord savedRecord = voiceRecordRepository.save(record);

        // 4. 반환
        return new SttResponse(savedRecord.getId(), savedRecord.getContent());
    }

    // 새로고침해도 기록이 보이게: 내 기록 목록 조회
    @Transactional(readOnly = true)
    public List<SttRecordResponse> getMyRecords(User user) {
        List<VoiceRecord> records = voiceRecordRepository.findAllByUserOrderByCreatedAtDesc(user);

        return records.stream()
                .map(r -> new SttRecordResponse(
                        r.getId(),
                        extractOriginalFilename(r.getAudioS3Key()),
                        r.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        normalizeContent(r.getContent()) // 과거에 {"text":...}로 저장된 값도 방어
                ))
                .toList();
    }

    // S3 URL에서 "원본 파일명" 추출 (uuid_파일명 구조)
    private String extractOriginalFilename(String s3Url) {
        if (s3Url == null || s3Url.isBlank()) return "audio";

        int lastSlash = s3Url.lastIndexOf('/');
        String key = (lastSlash >= 0) ? s3Url.substring(lastSlash + 1) : s3Url;

        int firstUnderscore = key.indexOf('_');
        String name = (firstUnderscore >= 0 && firstUnderscore < key.length() - 1)
                ? key.substring(firstUnderscore + 1)
                : key;

        try {
            return URLDecoder.decode(name, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return name;
        }
    }

    // 예전 데이터(content가 {"text":"..."} 형태)도 텍스트만 보여주도록 보정
    private String normalizeContent(String content) {
        if (content == null) return "";
        String trimmed = content.trim();

        if (trimmed.startsWith("{") && trimmed.contains("\"text\"")) {
            try {
                return objectMapper.readTree(trimmed).path("text").asText(trimmed);
            } catch (Exception ignored) {
            }
        }
        return content;
    }
}
