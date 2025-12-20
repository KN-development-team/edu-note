package com.edu.edu_note.domain.stt.service;

import com.edu.edu_note.domain.stt.entity.VoiceRecord;
import com.edu.edu_note.domain.stt.repository.VoiceRecordRepository;
import com.edu.edu_note.domain.user.entity.User;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttService {

    private final S3Template s3Template;
    private final WebClient.Builder webClientBuilder;
    private final VoiceRecordRepository voiceRecordRepository;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public String convertVoiceToText(User user, MultipartFile file) throws IOException {

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

        String resultJson = webClient.post()
                .uri("/stt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 3. DB 저장 (VoiceRecord)
        VoiceRecord record = VoiceRecord.builder()
                .user(user)
                .audioS3Key(s3Url)
                .content(resultJson)
                .build();

        voiceRecordRepository.save(record);

        return resultJson;
    }
}