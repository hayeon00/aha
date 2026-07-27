package com.aha.domain.ailearn.document.service.content;

import com.aha.domain.ailearn.document.client.content.AiConceptGenerationClient;
import com.aha.domain.ailearn.document.dto.content.response.DocumentConceptDashboardResponseDto;
import com.aha.domain.ailearn.document.dto.content.response.OwnedDocumentResponseDto;
import com.aha.domain.ailearn.document.dto.content.response.UserDocumentConceptResponseDto;
import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.entity.UserDocumentConcept;
import com.aha.domain.ailearn.document.enums.UserDocumentConceptSourceType;
import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.ailearn.document.repository.SourceDocumentRepository;
import com.aha.domain.ailearn.document.repository.UserDocumentConceptRepository;
import com.aha.domain.ailearn.document.repository.UserLearningContentRepository;
import com.aha.domain.ailearn.document.service.content.model.GeneratedLearningContent;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.enums.ExamScopeNodeType;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDocumentConceptService {
    private final SourceDocumentRepository documentRepository;
    private final ExamScopeNodeRepository tocRepository;
    private final DocumentScopeMappingRepository mappingRepository;
    private final UserDocumentConceptRepository conceptRepository;
    private final UserLearningContentRepository learningContentRepository;
    private final AiConceptGenerationClient generationClient;
    private final LearningContentBatchGenerationService learningContentBatchGenerationService;

    @Transactional(readOnly = true)
    public List<OwnedDocumentResponseDto> getOwnedDocuments(Long userId, Long userExamId) {
        validateIds(userId, userExamId);
        return documentRepository
                .findAllByProcessingGroup_UserExam_IdAndProcessingGroup_UserExam_User_IdOrderByIdDesc(
                        userExamId, userId)
                .stream().map(OwnedDocumentResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public DocumentConceptDashboardResponseDto getDashboard(Long userId, Long documentId) {
        SourceDocument document = getOwnedDocument(userId, documentId);
        List<ExamScopeNode> topics = getTopics(document);
        Set<Long> mappedIds = mappingRepository.findMappedTocIdsByDocumentId(documentId);
        Map<Long, UserDocumentConcept> concepts = conceptRepository
                .findAllByUser_IdAndDocument_IdOrderByToc_DisplayOrderAsc(userId, documentId)
                .stream().collect(Collectors.toMap(c -> c.getToc().getId(), Function.identity()));
        Map<Long, String> documentContents = learningContentRepository
                .findAllByUserExam_IdOrderByExamScopeNode_DisplayOrderAsc(
                        document.getProcessingGroup().getUserExam().getId())
                .stream().collect(Collectors.toMap(
                        content -> content.getExamScopeNode().getId(),
                        content -> content.getContent(),
                        (first, ignored) -> first));

        List<DocumentConceptDashboardResponseDto.TocConceptItem> mapped = new ArrayList<>();
        List<DocumentConceptDashboardResponseDto.TocConceptItem> unmapped = new ArrayList<>();
        for (int index = 0; index < topics.size(); index++) {
            ExamScopeNode topic = topics.get(index);
            UserDocumentConcept concept = concepts.get(topic.getId());
            var item = new DocumentConceptDashboardResponseDto.TocConceptItem(
                    topic.getId(), topic.getTitle(), index, concept != null,
                    mappedIds.contains(topic.getId())
                            ? UserDocumentConceptSourceType.DOCUMENT_MAPPED
                            : UserDocumentConceptSourceType.AI_GENERATED,
                    mappedIds.contains(topic.getId())
                            ? documentContents.get(topic.getId())
                            : concept == null ? null : concept.getContent(),
                    concept == null ? null : UserDocumentConceptResponseDto.from(concept));
            (mappedIds.contains(topic.getId()) ? mapped : unmapped).add(item);
        }
        return new DocumentConceptDashboardResponseDto(
                documentId, document.getOriginalFileName(), mapped, unmapped);
    }

    @Transactional
    public UserDocumentConceptResponseDto generate(Long userId, Long documentId, Long tocId) {
        return generate(userId, documentId, tocId, null);
    }

    @Transactional
    public UserDocumentConceptResponseDto generate(
            Long userId, Long documentId, Long tocId, String customPrompt) {
        validateIds(userId, documentId, tocId);
        if (customPrompt != null && customPrompt.length() > 1000) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        // 문서 행 잠금이 동일 tenant/document/toc 요청을 직렬화한다.
        SourceDocument document = documentRepository.findOwnedByIdForUpdate(documentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND));
        UserDocumentConcept cached = conceptRepository
                .findByUser_IdAndDocument_IdAndToc_Id(userId, documentId, tocId)
                .orElse(null);
        if (cached != null && (customPrompt == null || customPrompt.isBlank())) {
            return UserDocumentConceptResponseDto.from(cached);
        }

        ExamScopeNode topic = getValidTopic(document, tocId);
        if (mappingRepository.findMappedTocIdsByDocumentId(documentId).contains(tocId)) {
            throw new BusinessException(ErrorCode.INVALID_LEARNING_CONTENT_TARGET);
        }
        GeneratedLearningContent generated = generationClient.generate(
                topic.getTitle(), buildParentContext(topic), customPrompt);
        UserDocumentConcept saved;
        if (cached == null) {
            saved = conceptRepository.save(UserDocumentConcept.aiGenerated(
                    document.getProcessingGroup().getUserExam().getUser(), document, topic,
                    generated.title(), generated.body()));
        } else {
            cached.replaceGeneratedContent(generated.title(), generated.body());
            saved = conceptRepository.save(cached);
        }
        return UserDocumentConceptResponseDto.from(saved);
    }

    @Transactional
    public UserDocumentConceptResponseDto update(
            Long userId, Long documentId, Long tocId, String content) {
        validateIds(userId, documentId, tocId);
        if (content == null || content.isBlank() || content.length() > 50000) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        getOwnedDocument(userId, documentId);
        UserDocumentConcept concept = conceptRepository
                .findByUser_IdAndDocument_IdAndToc_Id(userId, documentId, tocId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_LEARNING_CONTENT_NOT_FOUND));
        if (concept.getSourceType() != UserDocumentConceptSourceType.AI_GENERATED) {
            throw new BusinessException(ErrorCode.INVALID_LEARNING_CONTENT_TARGET);
        }
        concept.updateContent(content);
        return UserDocumentConceptResponseDto.from(conceptRepository.save(concept));
    }

    @Transactional
    public void createLearningNote(Long userId, Long documentId) {
        SourceDocument document = documentRepository.findOwnedByIdForUpdate(documentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND));
        learningContentBatchGenerationService.generate(document.getProcessingGroup().getId());
    }

    @Transactional
    public List<UserDocumentConceptResponseDto> generateMissing(Long userId, Long documentId) {
        SourceDocument document = documentRepository.findOwnedByIdForUpdate(documentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND));
        return generateMissingConcepts(userId, document);
    }

    private List<UserDocumentConceptResponseDto> generateMissingConcepts(
            Long userId, SourceDocument document) {
        Long documentId = document.getId();
        Set<Long> mapped = mappingRepository.findMappedTocIdsByDocumentId(documentId);
        Map<Long, UserDocumentConcept> existing = conceptRepository
                .findAllByUser_IdAndDocument_IdOrderByToc_DisplayOrderAsc(userId, documentId)
                .stream().collect(Collectors.toMap(c -> c.getToc().getId(), Function.identity()));
        List<UserDocumentConceptResponseDto> results = new ArrayList<>();
        for (ExamScopeNode topic : getTopics(document)) {
            if (mapped.contains(topic.getId())) continue;
            UserDocumentConcept concept = existing.get(topic.getId());
            if (concept == null) {
                GeneratedLearningContent generated = generationClient.generate(
                        topic.getTitle(), buildParentContext(topic));
                concept = conceptRepository.save(UserDocumentConcept.aiGenerated(
                        document.getProcessingGroup().getUserExam().getUser(), document, topic,
                        generated.title(), generated.body()));
            }
            results.add(UserDocumentConceptResponseDto.from(concept));
        }
        return results;
    }

    private SourceDocument getOwnedDocument(Long userId, Long documentId) {
        validateIds(userId, documentId);
        return documentRepository.findById(documentId)
                .filter(d -> d.getProcessingGroup().getUserExam().getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND));
    }

    private List<ExamScopeNode> getTopics(SourceDocument document) {
        return tocRepository.findActiveNodesByExamVersionIdAndNodeTypes(
                document.getProcessingGroup().getUserExam().getExamVersion().getId(),
                List.of(ExamScopeNodeType.TOPIC));
    }

    private ExamScopeNode getValidTopic(SourceDocument document, Long tocId) {
        ExamScopeNode topic = tocRepository.findById(tocId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND));
        if (!topic.isActive() || topic.getNodeType() != ExamScopeNodeType.TOPIC
                || !topic.getExamVersion().getId().equals(
                document.getProcessingGroup().getUserExam().getExamVersion().getId())) {
            throw new BusinessException(ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND);
        }
        return topic;
    }

    private String buildParentContext(ExamScopeNode topic) {
        Deque<String> path = new ArrayDeque<>();
        for (ExamScopeNode current = topic.getParent(); current != null; current = current.getParent()) {
            path.addFirst(current.getTitle());
        }
        return path.isEmpty() ? "상위 목차 없음" : String.join(" > ", path);
    }

    private void validateIds(Long... ids) {
        if (Arrays.stream(ids).anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
