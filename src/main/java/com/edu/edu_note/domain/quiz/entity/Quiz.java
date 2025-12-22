//package com.edu.edu_note.domain.quiz.entity;
//
//import com.edu.edu_note.domain.stt.entity.VoiceRecord;
//import com.edu.edu_note.global.common.BaseTimeEntity;
//import jakarta.persistence.*;
//import lombok.AccessLevel;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Table(name = "quiz")
//public class Quiz extends BaseTimeEntity {
//
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "record_id") // record_id로 저장
//    private VoiceRecord voiceRecord;
//
//    @Column(columnDefinition = "TEXT")
//    private String content;
//
//    @Builder
//    public Quiz(VoiceRecord voiceRecord, String content) {
//        this.voiceRecord = voiceRecord;
//        this.content = content;
//    }
//}


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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private VoiceRecord voiceRecord;

    // --- DB의 type 컬럼 매핑 ---
    @Column(nullable = false)
    private String type;

    // --- DB의 level 컬럼 매핑 (DTO에서는 difficulty로 받으므로 이름 매핑 필요) ---
    @Column(name = "level", nullable = false)
    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public Quiz(VoiceRecord voiceRecord, String type, String difficulty, String content) {
        this.voiceRecord = voiceRecord;
        this.type = type;
        this.difficulty = difficulty;
        this.content = content;
    }
}