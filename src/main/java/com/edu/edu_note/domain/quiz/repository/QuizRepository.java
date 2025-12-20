package com.edu.edu_note.domain.quiz.repository;

import com.edu.edu_note.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // 특정 STT 노트(녹음본)에 딸린 퀴즈 목록을 불러오는 기능
    // 예: "1번 노트에서 만든 퀴즈 다 가져와"
//    List<Quiz> findAllBySttNoteId(Long sttNoteId);
}
