//package com.edu.edu_note.domain.stt.service;
//
//import io.awspring.cloud.s3.S3Template;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.MediaType;
//import org.springframework.http.client.MultipartBodyBuilder;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.reactive.function.BodyInserters;
//
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class SttService {
//    private final S3Template s3Template; // S3 업로드 도구
//    private final WebClient.Builder webClientBuilder; // AI 서버 통신 도구
//
//    @Value("${ai.server.url}")
//    private String aiServerUrl;
//
//    @Value("${spring.cloud.aws.s3.bucket}")
//    private String bucketName;
//
//    public String convertVoiceToText(MultipartFile file) throws IOException {
//        // 1. S3에 파일 업로드 (파일 이름 중복 방지를 위해 UUID 사용)
//        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//        String s3Url = s3Template.upload(bucketName, fileName, file.getInputStream()).getURL().toString();
//
//        System.out.println("S3 업로드 완료: " + s3Url);
//
//        // 2. AI 서버로 파일 전송해서 텍스트 받아오기
//        // (S3 URL만 줘도 되지만, 지금 FastAPI 구조상 파일을 직접 보내줍니다)
//        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();
//
//        MultipartBodyBuilder builder = new MultipartBodyBuilder();
//        builder.part("file", new ByteArrayResource(file.getBytes()) {
//            @Override
//            public String getFilename() {
//                return file.getOriginalFilename();
//            }
//        });
//
//        // AI 서버의 /stt 엔드포인트 호출
//        String resultText = webClient.post()
//                .uri("/stt")
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .body(BodyInserters.fromMultipartData(builder.build()))
//                .retrieve()
//                .bodyToMono(String.class) // 결과를 String(JSON)으로 받음
//                .block(); // 결과 올 때까지 기다림
//
//        // 실제로는 여기서 JSON 파싱을 해야 하지만, 일단 문자열로 반환
//        return resultText;
//    }
//}



package com.edu.edu_note.domain.stt.service;

import com.edu.edu_note.domain.stt.entity.SttNote;
import com.edu.edu_note.domain.stt.repository.SttNoteRepository;
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
    private final SttNoteRepository sttNoteRepository; // DB 저장을 위해 추가

    @Value("${ai.server.url}") // application.yml에 설정된 주소 (http://localhost:8000)
    private String aiServerUrl;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional // DB 저장이 있으므로 트랜잭션 처리
    public String convertVoiceToText(User user, MultipartFile file) throws IOException {

        // 1. S3에 파일 업로드
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String s3Url = s3Template.upload(bucketName, fileName, file.getInputStream()).getURL().toString();
        log.info("S3 Upload Success: {}", s3Url);

        // 2. AI 서버(FastAPI)로 파일 전송 및 변환 요청
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
                .block(); // 결과가 올 때까지 기다림 (동기 처리)

        // 3. 변환 결과 DB에 저장 (SttNote)
        SttNote note = SttNote.builder()
                .user(user)          // 로그인한 사용자 정보
                .audioUrl(s3Url)     // 녹음 파일 주소
                .content(resultJson) // 변환된 텍스트 내용 (JSON 그대로 저장하거나 파싱해서 저장)
                .build();

        sttNoteRepository.save(note); // 저장!

        return resultJson;
    }
}