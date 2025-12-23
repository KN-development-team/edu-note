package com.edu.edu_note.domain.quiz.repository;

import com.edu.edu_note.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // 특정 녹음(record_id)에 대한 퀴즈 목록 (최신순)
    List<Quiz> findAllByVoiceRecord_IdOrderByCreatedAtDesc(Long recordId);
}
