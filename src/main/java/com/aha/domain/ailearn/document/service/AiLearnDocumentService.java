package com.aha.domain.ailearn.document.service;

import com.aha.domain.ailearn.document.dto.response.DocumentBatchUploadResponseDto;
import com.aha.domain.ailearn.document.dto.response.DocumentProcessingGroupResponseDto;
import com.aha.domain.ailearn.document.dto.response.UploadedDocumentResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStatus;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import com.aha.domain.ailearn.document.enums.SourceDocumentStatus;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.DocumentProcessingRepository;
import com.aha.domain.ailearn.document.repository.SourceDocumentRepository;
import com.aha.domain.user.entity.UserExam;
import com.aha.domain.user.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiLearnDocumentService {

    private static final long MAX_DOCUMENT_FILE_SIZE = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS =
            Set.of("pdf", "docx", "txt");

    private final UserExamRepository userExamRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final DocumentProcessingRepository documentProcessingRepository;
    private final DocumentProcessingGroupRepository documentProcessingGroupRepository;
    private final DocumentProcessingWorkerService documentProcessingWorkerService;

    @Value("${file.document-upload-dir:uploads/documents}")
    private String documentUploadDir;

    @Transactional
    public DocumentBatchUploadResponseDto uploadDocumentsBatch(
            Long userId,
            Long userExamId,
            List<MultipartFile> files
    ) {

        UserExam userExam = userExamRepository.findByIdAndUser_Id(userExamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_EXAM_NOT_FOUND));

        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }

        //파일 목록안에 null이거나 빈파일인 경우 제거
        List<MultipartFile> validFiles = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        // 다시 확인해서 유효한 파일이 0개이면 에러
        if (validFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }

        // 각 파일이 유효한 파일인지 확인
        for (MultipartFile file : validFiles) {
            validateDocumentFile(file);
        }

        DocumentProcessingGroup processingGroup = DocumentProcessingGroup.builder()
                .userExam(userExam)
                .status(DocumentProcessingStatus.PENDING)
                .currentStep(DocumentProcessingStep.FILE_UPLOADED)
                .progressRate(DocumentProcessingStep.FILE_UPLOADED.getProgressRate())
                .totalFileCount(validFiles.size())
                .completedFileCount(0)
                .failedFileCount(0)
                .build();

        DocumentProcessingGroup savedProcessingGroup =
                documentProcessingGroupRepository.save(processingGroup);


        // 응답용 리스트 생성
        List<UploadedDocumentResponseDto> uploadedDocuments = new ArrayList<>();


        // 파일 반복 저장
        for (MultipartFile file : validFiles) {
            try {
                String originalFileName = file.getOriginalFilename();
                String extension = originalFileName
                        .substring(originalFileName.lastIndexOf(".") + 1)
                        .toLowerCase();

                String storedFileName = UUID.randomUUID() + "." + extension;

                // 저장 경로 생성
                Path uploadPath = Paths
                        .get(
                                documentUploadDir,
                                String.valueOf(userId),
                                String.valueOf(userExamId),
                                String.valueOf(savedProcessingGroup.getId())
                        )
                        .toAbsolutePath()
                        .normalize();

                // 해당 폴더가 없으면 자동 생성
                Files.createDirectories(uploadPath);


                // 실제 파일 저장
                Path targetPath = uploadPath
                        .resolve(storedFileName)
                        .toAbsolutePath()
                        .normalize();

                file.transferTo(targetPath.toFile());


                // storageKey 생성(DB에 저장할 상대 경로
                String storageKey = "documents/"
                        + userId + "/"
                        + userExamId + "/"
                        + savedProcessingGroup.getId() + "/"
                        + storedFileName;


                //source_document 저장
                SourceDocument sourceDocument = SourceDocument.builder()
                        .userExam(userExam)
                        .originalFileName(originalFileName)
                        .storedFileName(storedFileName)
                        .storageKey(storageKey)
                        .fileExtension(extension.toUpperCase())
                        .mimeType(file.getContentType() != null
                                ? file.getContentType()
                                : "application/octet-stream")
                        .fileSize(file.getSize())
                        .status(SourceDocumentStatus.UPLOADED)
                        .isActive(true)
                        .build();

                SourceDocument savedSourceDocument =
                        sourceDocumentRepository.save(sourceDocument);


                // 파일별 처리 상태(파일이 3개면 처리작업도 3개)
                DocumentProcessing documentProcessing = DocumentProcessing.builder()
                        .processingGroup(savedProcessingGroup)
                        .sourceDocument(savedSourceDocument)
                        .status(DocumentProcessingStatus.PENDING)
                        .build();

                DocumentProcessing savedDocumentProcessing =
                        documentProcessingRepository.save(documentProcessing);

                // 응답 리스트에 추가
                uploadedDocuments.add(
                        UploadedDocumentResponseDto.of(
                                savedSourceDocument,
                                savedDocumentProcessing
                        )
                );
            } catch (Exception e) {
                log.error(
                        "학습 문서 업로드 실패. userId={}, userExamId={}, processingGroupId={}",
                        userId,
                        userExamId,
                        savedProcessingGroup.getId(),
                        e
                );

                savedProcessingGroup.fail("문서 업로드 중 오류가 발생했습니다.");

                throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
            }
        }

        startProcessingAfterCommit(savedProcessingGroup.getId());

        // 최종 응답 반환
        return DocumentBatchUploadResponseDto.of(
                savedProcessingGroup,
                uploadedDocuments
        );
    }

    private void startProcessingAfterCommit(Long processingGroupId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                documentProcessingWorkerService.process(processingGroupId);
            }
        });
    }


    public DocumentProcessingGroupResponseDto getProcessingGroup(
            Long userId,
            Long processingGroupId
    ) {
        DocumentProcessingGroup processingGroup =
                documentProcessingGroupRepository
                        .findByIdAndUserExam_User_Id(processingGroupId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));

        return DocumentProcessingGroupResponseDto.from(processingGroup);
    }


    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }

        if (file.getSize() > MAX_DOCUMENT_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank() || !originalFileName.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }

        String extension = originalFileName
                .substring(originalFileName.lastIndexOf(".") + 1)
                .toLowerCase();

        if (!ALLOWED_DOCUMENT_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_FILE);
        }
    }
}
