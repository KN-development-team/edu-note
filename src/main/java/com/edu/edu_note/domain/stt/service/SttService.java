package com.edu.edu_note.domain.stt.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;


import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SttService {
    private final S3Template s3Template; // S3 업로드 도구
    private final WebClient.Builder webClientBuilder; // AI 서버 통신 도구

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public String convertVoiceToText(MultipartFile file) throws IOException {
        // 1. S3에 파일 업로드 (파일 이름 중복 방지를 위해 UUID 사용)
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String s3Url = s3Template.upload(bucketName, fileName, file.getInputStream()).getURL().toString();

        System.out.println("S3 업로드 완료: " + s3Url);

        // 2. AI 서버로 파일 전송해서 텍스트 받아오기
        // (S3 URL만 줘도 되지만, 지금 FastAPI 구조상 파일을 직접 보내줍니다)
        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        // AI 서버의 /stt 엔드포인트 호출
        String resultText = webClient.post()
                .uri("/stt")
//                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class) // 결과를 String(JSON)으로 받음
                .block(); // 결과 올 때까지 기다림

        // 실제로는 여기서 JSON 파싱을 해야 하지만, 일단 문자열로 반환
        return resultText;
    }
}
