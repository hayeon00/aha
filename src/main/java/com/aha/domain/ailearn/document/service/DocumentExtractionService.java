package com.aha.domain.ailearn.document.service;

import com.aha.domain.ailearn.document.dto.response.DocumentExtractionResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.ExtractedContent;
import com.aha.domain.ailearn.document.entity.LearningSourceDocument;
import com.aha.domain.ailearn.document.repository.DocumentProcessingRepository;
import com.aha.domain.ailearn.document.repository.ExtractedContentRepository;
import com.aha.domain.ailearn.document.repository.LearningSourceDocumentRepository;
import com.aha.domain.ailearn.document.type.ProcessingStatus;
import com.aha.domain.ailearn.document.type.ProcessingType;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentExtractionService {

    private final LearningSourceDocumentRepository sourceDocumentRepository;
    private final DocumentProcessingRepository processingRepository;
    private final ExtractedContentRepository extractedContentRepository;
    private final PdfTextExtractionService pdfTextExtractionService;

    @Transactional
    public DocumentExtractionResponseDto extractText(Long sourceDocumentId, Long requestedBy) {
        LearningSourceDocument sourceDocument = sourceDocumentRepository.findById(sourceDocumentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND));

        DocumentProcessing processing = DocumentProcessing.builder()
                .sourceDocumentId(sourceDocument.getId())
                .processingType(ProcessingType.TEXT_EXTRACTION)
                .status(ProcessingStatus.PENDING)
                .requestedBy(requestedBy)
                .build();

        DocumentProcessing savedProcessing = processingRepository.save(processing);

        try {
            savedProcessing.start();

            List<PdfTextExtractionService.ExtractedPageText> pageTexts =
                    pdfTextExtractionService.extractByPage(sourceDocument.getFilePath());

            if (pageTexts.isEmpty()) {
                throw new BusinessException(ErrorCode.DOCUMENT_EXTRACTION_FAILED);
            }

            int chunkOrder = 1;

            for (PdfTextExtractionService.ExtractedPageText pageText : pageTexts) {
                ExtractedContent extractedContent = ExtractedContent.builder()
                        .sourceDocumentId(sourceDocument.getId())
                        .processingId(savedProcessing.getId())
                        .examScopeNodeId(sourceDocument.getExamScopeNodeId())
                        .chunkOrder(chunkOrder++)
                        .pageNo(pageText.getPageNo())
                        .contentText(pageText.getText())
                        .isUsedForLearning(true)
                        .isUsedForRag(true)
                        .build();

                extractedContentRepository.save(extractedContent);
            }

            savedProcessing.complete();

            return new DocumentExtractionResponseDto(
                    sourceDocument.getId(),
                    savedProcessing.getId(),
                    pageTexts.size(),
                    savedProcessing.getStatus().name()
            );

        } catch (BusinessException e) {
            savedProcessing.fail(e.getMessage());
            throw e;
        } catch (Exception e) {
            savedProcessing.fail(e.getMessage());
            throw new BusinessException(ErrorCode.DOCUMENT_EXTRACTION_FAILED);
        }
    }
}