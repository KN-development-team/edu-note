package com.edu.edu_note.domain.summary.repository;

import com.edu.edu_note.domain.stt.entity.VoiceRecord;
import com.edu.edu_note.domain.summary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> { // Summary 엔티티, PK타입은 LONG
    // Summary 엔티티에서 recordId로 summary 객체 추출
    Optional<Summary> findByRecord_Id(Long recordId); // summary.record.id = ? (여기서 record는 summary에 저장된 record 객체)
}
