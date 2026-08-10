package com.aha.domain.learningnote.service;

import com.aha.domain.document.entity.DocumentProcessing;
import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentFileExtension;
import com.aha.domain.document.event.DocumentProcessingRequestedEvent;
import com.aha.domain.document.repository.DocumentProcessingRepository;
import com.aha.domain.document.repository.SourceDocumentRepository;
import com.aha.domain.document.model.ValidatedDocumentFile;
import com.aha.domain.document.model.StoredDocumentFile;
import com.aha.domain.document.service.DocumentFileStorageService;
import com.aha.domain.document.service.DocumentFileValidator;
import com.aha.domain.learningnote.dto.response.LearningNoteCreateResponseDto;
import com.aha.domain.learningnote.entity.LearningNote;
import com.aha.domain.learningnote.repository.LearningNoteRepository;
import com.aha.domain.userexam.entity.UserExam;
import com.aha.domain.userexam.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningNoteCreateService {

    private final UserExamRepository userExamRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final LearningNoteRepository learningNoteRepository;
    private final DocumentProcessingRepository documentProcessingRepository;

    private final DocumentFileValidator documentFileValidator;
    private final DocumentFileStorageService documentFileStorageService;

    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public LearningNoteCreateResponseDto create(Long userId, Long userExamId, String title, MultipartFile file) {

        UserExam userExam = getOwnedUserExam(userId, userExamId);

        ValidatedDocumentFile validatedFile = documentFileValidator.validate(file);

        StoredDocumentFile storedFile = null;

        try{
            storedFile = documentFileStorageService.store(
                    userId,
                    userExamId,
                    validatedFile
            );

            SourceDocument sourceDocument = sourceDocumentRepository.save(
                    SourceDocument.create(
                            userExam.getUser(),
                            validatedFile.originalFileName(),
                            storedFile.storedFileName(),
                            storedFile.storageKey(),
                            DocumentFileExtension.valueOf(validatedFile.fileExtension()),
                            validatedFile.mimeType(),
                            validatedFile.fileSize()
                    )
            );

            LearningNote learningNote = learningNoteRepository.save(
                    LearningNote.create(
                            userExam,
                            sourceDocument,
                            title
                    )
            );

            DocumentProcessing documentProcessing =
                    documentProcessingRepository.save(
                            DocumentProcessing.createPending(
                                    learningNote,
                                    "rag-note-v1",
                                    1
                            )
                    );

            eventPublisher.publishEvent(
                    new DocumentProcessingRequestedEvent(
                            documentProcessing.getId()
                    )
            );

            return LearningNoteCreateResponseDto.from(
                    learningNote,
                    documentProcessing
            );

        }catch (Exception exception){

            deleteStoredFileSafely(storedFile);

            if(exception instanceof BusinessException businessException){
                throw businessException;
            }

            log.error(
                    "학습 노트 생성 중 오류가 발생했습니다.",
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }

    private void deleteStoredFileSafely(StoredDocumentFile storedFile) {

        if(storedFile == null) return;

        try{
            documentFileStorageService.delete(
                    storedFile.storageKey()
            );
        }catch (Exception cleanupException){
            log.error(
                    "학습 노트 생성 실패 후 파일 정리에 실패했습니다. storageKey={}",
                    storedFile.storageKey(),
                    cleanupException
            );
        }
    }


    private UserExam getOwnedUserExam(Long userId, Long userExamId) {

        return userExamRepository.findByIdAndUser_Id(userExamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_EXAM_NOT_FOUND));
    }
}
