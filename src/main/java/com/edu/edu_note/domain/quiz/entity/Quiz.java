package com.edu.edu_note.domain.quiz.entity;

import com.edu.edu_note.domain.stt.entity.VoiceRecord;
import com.edu.edu_note.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quiz")
public class Quiz extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // record_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private VoiceRecord voiceRecord;

    @Column(nullable = false)
    private String type;

    // DB 컬럼명 level
    @Column(name = "level", nullable = false)
    private String level;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public Quiz(VoiceRecord voiceRecord, String type, String level, String content) {
        this.voiceRecord = voiceRecord;
        this.type = type;
        this.level = level;
        this.content = content;
    }
}

