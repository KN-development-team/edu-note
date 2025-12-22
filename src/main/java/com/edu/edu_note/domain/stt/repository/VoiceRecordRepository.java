package com.edu.edu_note.domain.stt.repository;

import com.edu.edu_note.domain.stt.entity.VoiceRecord;
import com.edu.edu_note.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoiceRecordRepository extends JpaRepository<VoiceRecord, Long> {

    // 내 기록 최신순 조회
    List<VoiceRecord> findAllByUserOrderByCreatedAtDesc(User user);
}
