package com.edu.edu_note.domain.stt.entity;

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
@Table(name = "voice_records")
public class VoiceRecord extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "audio_s3_key", nullable = false)
    private String audioS3Key;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_record_user")
    )
    private User user;

    @Builder
    public VoiceRecord(User user, String audioS3Key, String content) {
        this.user = user;
        this.audioS3Key = audioS3Key;
        this.content = content;
    }
}