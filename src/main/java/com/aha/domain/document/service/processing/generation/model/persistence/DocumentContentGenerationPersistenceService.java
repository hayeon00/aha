package com.aha.domain.document.service.processing.generation.model.persistence;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.document.service.processing.generation.model.TopicContentSource;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;
import com.aha.domain.learningnote.entity.LearningContentReference;
import com.aha.domain.learningnote.entity.LearningNote;
import com.aha.domain.learningnote.entity.LearningNoteContent;
import com.aha.domain.learningnote.repository.LearningContentReferenceRepository;
import com.aha.domain.learningnote.repository.LearningNoteContentRepository;
import com.aha.domain.learningnote.repository.LearningNoteRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentContentGenerationPersistenceService {

    private static final String GENERATION_VERSION = "concept-v1";

    private final LearningNoteRepository learningNoteRepository;
    private final LearningNoteContentRepository learningNoteContentRepository;
    private final LearningContentReferenceRepository learningContentReferenceRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final DocumentChunkRepository documentChunkRepository;

    @Transactional
    public void saveGeneratedContent(
            Long learningNoteId,
            TopicContentSource source,
            ConceptExplanationResult result
    ) {
        LearningNote note = learningNoteRepository.findById(learningNoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_NOTE_NOT_FOUND));
        ExamScopeNode scopeNode = examScopeNodeRepository.findById(source.scopeNodeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND));

        LearningNoteContent content = learningNoteContentRepository
                .findByLearningNote_IdAndExamScopeNode_Id(learningNoteId, source.scopeNodeId())
                .map(existing -> {
                    existing.updateGeneratedContent(
                            result.title(),
                            result.content(),
                            null,
                            GENERATION_VERSION
                    );
                    return existing;
                })
                .orElseGet(() -> LearningNoteContent.createDocumentBased(
                        note,
                        scopeNode,
                        result.title(),
                        result.content(),
                        null,
                        GENERATION_VERSION
                ));

        LearningNoteContent savedContent = learningNoteContentRepository.save(content);
        learningContentReferenceRepository
                .deleteAllByLearningNoteContent_Id(savedContent.getId());

        Map<Long, DocumentChunk> chunksById = documentChunkRepository
                .findAllById(source.chunks().stream()
                        .map(chunk -> chunk.documentChunkId())
                        .toList())
                .stream()
                .collect(Collectors.toMap(DocumentChunk::getId, Function.identity()));

        var references = IntStream
                .range(0, source.chunks().size())
                .mapToObj(index -> {
                    Long chunkId = source.chunks().get(index).documentChunkId();
                    DocumentChunk chunk = chunksById.get(chunkId);
                    if (chunk == null) {
                        throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
                    }
                    return LearningContentReference.create(
                            savedContent,
                            chunk,
                            index + 1,
                            null
                    );
                })
                .toList();

        learningContentReferenceRepository.saveAll(references);
    }

    @Transactional
    public void markLearningNoteReady(Long learningNoteId) {
        LearningNote note = learningNoteRepository.findById(learningNoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_NOTE_NOT_FOUND));
        note.markReady();
    }
}
