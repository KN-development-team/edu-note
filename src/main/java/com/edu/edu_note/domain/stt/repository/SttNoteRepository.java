package com.edu.edu_note.domain.stt.repository;

import com.edu.edu_note.domain.stt.entity.SttNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SttNoteRepository extends JpaRepository<SttNote, Long> {
    // 특정 사용자의 노트만 조회하는 기능
    List<SttNote> findAllByUserId(Long userId);
}
