package com.aha.domain.ailearn.document.entity;

import com.aha.domain.user.entity.UserExam;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "source_document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_exam_id", nullable = false)
    private UserExam userExam;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "file_extension", nullable = false, length = 20)
    private String fileExtension;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private SourceDocument(
            UserExam userExam,
            String originalFileName,
            String storedFileName,
            String storageKey,
            String fileExtension,
            String mimeType,
            Long fileSize,
            String status,
            Boolean isActive
    ) {
        this.userExam = userExam;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.storageKey = storageKey;
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.status = status != null ? status : "UPLOADED";
        this.isActive = isActive != null ? isActive : true;
    }

    public void updateStatus(String status) {
        this.status = status;
    }
}