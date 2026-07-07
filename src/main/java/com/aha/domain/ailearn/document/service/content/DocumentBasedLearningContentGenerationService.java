package com.aha.domain.ailearn.document.service.content;

import com.aha.domain.ailearn.document.client.content.LearningContentContextBuilder;
import com.aha.domain.ailearn.document.client.content.LearningContentGenerationClient;
import com.aha.domain.ailearn.document.dto.content.response.DocumentBasedLearningContentResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.service.mapping.DocumentScopeMappingQueryService;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.user.entity.UserExam;
import com.aha.domain.user.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentBasedLearningContentGenerationService
 * @since : 2026. 6. 25. 목요일
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentBasedLearningContentGenerationService {

    private final DocumentScopeMappingQueryService documentScopeMappingQueryService;
    private final LearningContentContextBuilder learningContentContextBuilder;
    private final LearningContentGenerationClient learningContentGenerationClient;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final UserExamRepository userExamRepository;
    private final UserLearningContentPersistenceService userLearningContentPersistenceService;

    public DocumentBasedLearningContentResponseDto generate(Long userId, Long userExamId, Long examScopeNodeId){

        validateInput(userId, userExamId, examScopeNodeId);

        UserExam userExam = userExamRepository.findByIdAndUser_Id(userExamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_EXAM_NOT_FOUND));

        ExamScopeNode scopeNode = examScopeNodeRepository.findById(examScopeNodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        validateScopeNodeBelongsToUserExam(userExam, scopeNode);

        validateLearningContentTarget(scopeNode);

        List<DocumentChunk> mappedChunks = documentScopeMappingQueryService.getMappedChunks(userExamId, examScopeNodeId);

        if(mappedChunks.isEmpty()){

            log.info(
                    "목차에 매핑된 문서 청크가 없습니다. userExamId={}, examScopeNodeId={}",
                    userExamId,
                    examScopeNodeId
            );

            return DocumentBasedLearningContentResponseDto.builder()
                    .examScopeNodeId(scopeNode.getId())
                    .topicTitle(scopeNode.getTitle())
                    .mappedChunkCount(0)
                    .content(null)
                    .build();
        }

        String documentContext = learningContentContextBuilder.build(mappedChunks);

        String generatedContent = learningContentGenerationClient.generate(scopeNode.getTitle(), documentContext);

        userLearningContentPersistenceService.saveOrUpdate(userExam, scopeNode, generatedContent);

        log.info(
                "문서 기반 개념 설명 생성 및 저장 완료. userId={}, userExamId={}, examScopeNodeId={}, mappedChunkCount={}",
                userId,
                userExamId,
                examScopeNodeId,
                mappedChunks.size()
        );

        return DocumentBasedLearningContentResponseDto.builder()
                .examScopeNodeId(scopeNode.getId())
                .topicTitle(scopeNode.getTitle())
                .mappedChunkCount(mappedChunks.size())
                .content(generatedContent)
                .build();

    }

    private void validateLearningContentTarget(ExamScopeNode scopeNode) {

        if(!scopeNode.isLeaf() || !scopeNode.isActive()){
            throw new BusinessException(ErrorCode.INVALID_LEARNING_CONTENT_TARGET);
        }
    }

    private void validateScopeNodeBelongsToUserExam(UserExam userExam, ExamScopeNode scopeNode) {

        Long userExamVersionId = userExam.getExamVersion().getId();

        Long scopeNodeExamVersionId = scopeNode.getExamVersion().getId();

        if(!userExamVersionId.equals(scopeNodeExamVersionId)){

            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

    }

    private void validateInput(Long userId, Long userExamId, Long examScopeNodeId) {

        if(userId == null || userExamId == null || examScopeNodeId == null){

            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
