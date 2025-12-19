package com.edu.edu_note.domain.summary.repository;

import com.edu.edu_note.domain.summary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> { // Summary 엔티티, PK타입은 LONG



}
