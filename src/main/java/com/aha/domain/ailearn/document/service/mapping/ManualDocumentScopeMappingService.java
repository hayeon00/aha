package com.aha.domain.ailearn.document.service.mapping;

import com.aha.domain.ailearn.document.dto.mapping.response.UnassignedDocumentChunkResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import com.aha.domain.ailearn.document.enums.DocumentChunkMappingStatus;
import com.aha.domain.ailearn.document.repository.DocumentChunkRepository;
import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ManualDocumentScopeMappingService {

    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentScopeMappingRepository mappingRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;

    @Transactional(readOnly = true)
    public List<UnassignedDocumentChunkResponseDto> getUnassignedChunks(
            Long userId,
            Long userExamId
    ) {
        validateId(userId);
        validateId(userExamId);

        return documentChunkRepository
                .findAllByUserExamAndMappingStatus(
                        userId,
                        userExamId,
                        DocumentChunkMappingStatus.UNASSIGNED
                )
                .stream()
                .map(UnassignedDocumentChunkResponseDto::from)
                .toList();
    }

    @Transactional
    public void assign(Long userId, Long chunkId, Long examScopeNodeId) {
        validateId(userId);
        validateId(chunkId);
        validateId(examScopeNodeId);

        DocumentChunk chunk = documentChunkRepository.findOwnedChunk(userId, chunkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND));
        ExamScopeNode scopeNode = examScopeNodeRepository.findById(examScopeNodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND));

        Long chunkExamVersionId = chunk.getSourceDocument().getProcessingGroup()
                .getUserExam().getExamVersion().getId();
        Long scopeExamVersionId = scopeNode.getExamVersion().getId();

        if (!Objects.equals(chunkExamVersionId, scopeExamVersionId)
                || !scopeNode.isLeaf()
                || !scopeNode.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        mappingRepository.deleteAllByDocumentChunk_Id(chunkId);
        mappingRepository.save(DocumentScopeMapping.builder()
                .documentChunk(chunk)
                .examScopeNode(scopeNode)
                .rankNo(1)
                .confidenceScore(BigDecimal.ONE.setScale(4))
                .mappingReason("사용자가 직접 지정한 목차입니다.")
                .build());
        chunk.markManualMapped();
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
