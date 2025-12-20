package com.edu.edu_note.domain.stt.entity;


import com.edu.edu_note.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
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
@Table(name = "stt_notes")
public class SttNote extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 녹음했는지 (User와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // S3에 올라간 파일 주소
    @Column(nullable = false)
    private String audioUrl;

    // 변환된 텍스트 (길 수 있으니 TEXT 타입)
    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public SttNote(User user, String audioUrl, String content) {
        this.user = user;
        this.audioUrl = audioUrl;
        this.content = content;
    }

}
