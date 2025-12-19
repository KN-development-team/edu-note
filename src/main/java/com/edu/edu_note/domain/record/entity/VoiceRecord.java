package com.edu.edu_note.domain.record.entity;

import com.edu.edu_note.domain.user.entity.User;
import com.edu.edu_note.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "records")
public class VoiceRecord extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 외래키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // 생성자
    @Builder
    public VoiceRecord(User user, String content) {
        this.user = user;
        this.content = content;
    }
}
