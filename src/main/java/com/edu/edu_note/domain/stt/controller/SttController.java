package com.edu.edu_note.domain.stt.controller;

import com.edu.edu_note.domain.stt.service.SttService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/stt")
@RequiredArgsConstructor
public class SttController {
    private final SttService sttService;

    @PostMapping
    public ResponseEntity<String> convertStt(@RequestParam("file") MultipartFile file) {
        try {
            String result = sttService.convertVoiceToText(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 처리 중 오류 발생: " + e.getMessage());
        }
    }
}
