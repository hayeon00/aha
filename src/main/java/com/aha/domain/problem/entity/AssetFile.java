package com.aha.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "asset_file",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_asset_file_code", columnNames = "code")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자 통제
public class AssetFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(name = "storage_provider", nullable = false, length = 30)
  private String storageProvider; // LOCAL, S3 등

  @Column(name = "bucket_name", length = 100)
  private String bucketName;

  @Column(name = "file_path", nullable = false, length = 500)
  private String filePath;

  @Column(name = "original_file_name", nullable = false, length = 255)
  private String originalFileName;

  @Column(name = "stored_file_name", nullable = false, length = 255)
  private String storedFileName;

  @Column(name = "file_extension", nullable = false, length = 10)
  private String fileExtension;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "mime_type", nullable = false, length = 100)
  private String mimeType;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public AssetFile(String code, String storageProvider, String bucketName, String filePath,
      String originalFileName, String storedFileName, String fileExtension,
      Long fileSize, String mimeType, Boolean isActive) {
    this.code = code;
    this.storageProvider = (storageProvider != null) ? storageProvider : "LOCAL";
    this.bucketName = bucketName;
    this.filePath = filePath;
    this.originalFileName = originalFileName;
    this.storedFileName = storedFileName;
    this.fileExtension = fileExtension;
    this.fileSize = fileSize;
    this.mimeType = mimeType;
    this.isActive = (isActive != null) ? isActive : true;
  }

  public void deactivate() {
    this.isActive = false;
  }
}