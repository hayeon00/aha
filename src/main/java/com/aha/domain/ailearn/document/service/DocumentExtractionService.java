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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentExtractionService {

    private final LearningSourceDocumentRepository sourceDocumentRepository;
    private final DocumentProcessingRepository processingRepository;
    private final ExtractedContentRepository extractedContentRepository;
    private final PdfTextExtractionService pdfTextExtractionService;

    public DocumentExtractionResponseDto extractText(Long sourceDocumentId, Long requestedBy) {
        LearningSourceDocument sourceDocument = sourceDocumentRepository.findById(sourceDocumentId)
                .orElseThrow(() -> new IllegalArgumentException("학습 원본문서를 찾을 수 없습니다. id=" + sourceDocumentId));

        DocumentProcessing processing = DocumentProcessing.builder()
                .sourceDocumentId(sourceDocument.getId())
                .processingType(ProcessingType.TEXT_EXTRACTION)
                .status(ProcessingStatus.PENDING)
                .requestedBy(requestedBy)
                .build();

        DocumentProcessing savedProcessing = processingRepository.save(processing);

        try {
            savedProcessing.start();
            processingRepository.save(savedProcessing);

            List<PdfTextExtractionService.ExtractedPageText> pageTexts =
                    pdfTextExtractionService.extractByPage(sourceDocument.getFilePath());

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
            processingRepository.save(savedProcessing);

            return new DocumentExtractionResponseDto(
                    sourceDocument.getId(),
                    savedProcessing.getId(),
                    pageTexts.size(),
                    savedProcessing.getStatus().name()
            );

        } catch (Exception e) {
            savedProcessing.fail(e.getMessage());
            processingRepository.save(savedProcessing);

            throw new IllegalStateException("학습 원본문서 텍스트 추출에 실패했습니다.", e);
        }
    }
}