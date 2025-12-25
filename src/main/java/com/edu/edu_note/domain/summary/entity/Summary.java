//package com.edu.edu_note.domain.summary.entity;
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
//@Table(name = "summary")
//public class Summary extends BaseTimeEntity {
//
//    @Id @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//
//    @Column(columnDefinition = "TEXT", nullable = false)
//    private String content;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "record_id",
//            nullable = false,
//            foreignKey = @ForeignKey(name = "fk_summary_record")
//    )
//
//    private VoiceRecord record; // Record 클래스
//
//    // 생성자
//    @Builder
//    public Summary(VoiceRecord record, String content) {
//        this.record = record;
//        this.content = content;
//    }
//
//
//}



package com.edu.edu_note.domain.summary.entity;

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
@Table(name = "summary")
public class Summary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "record_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_summary_record")
    )
    private VoiceRecord record;

    @Builder
    public Summary(VoiceRecord record, String content) {
        this.record = record;
        this.content = content;
    }

    // 기존 요약 재생성(업데이트)
    public void updateContent(String content) {
        this.content = content;
    }
}
