package com.aha.domain.document.service.processing.embedding;

import com.aha.domain.document.client.embedding.EmbeddingClient;
import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.DocumentChunkEmbedding;
import com.aha.domain.document.entity.ExamScopeNodeEmbedding;
import com.aha.domain.document.repository.DocumentChunkEmbeddingRepository;
import com.aha.domain.document.repository.ExamScopeNodeEmbeddingRepository;
import com.aha.domain.document.service.processing.embedding.util.EmbeddingJsonConverter;
import com.aha.domain.document.service.processing.embedding.util.EmbeddingTextBuilder;
import com.aha.domain.document.service.processing.embedding.util.EmbeddingTextHashGenerator;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEmbeddingService {

    private static final int EMBEDDING_BATCH_SIZE = 20;

    private final EmbeddingClient embeddingClient;
    private final EmbeddingModelProvider embeddingModelProvider;
    private final EmbeddingTextBuilder embeddingTextBuilder;
    private final EmbeddingTextHashGenerator embeddingTextHashGenerator;
    private final EmbeddingJsonConverter embeddingJsonConverter;
    private final DocumentChunkEmbeddingRepository documentChunkEmbeddingRepository;
    private final ExamScopeNodeEmbeddingRepository examScopeNodeEmbeddingRepository;

    @Transactional
    public void ensureChunkEmbeddings(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        String embeddingModel = embeddingModelProvider.getEmbeddingModel();

        List<Long> chunkIds = chunks.stream()
                .filter(Objects::nonNull)
                .map(DocumentChunk::getId)
                .filter(Objects::nonNull)
                .toList();

        if (chunkIds.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        Map<Long, DocumentChunkEmbedding> existingEmbeddingMap =
                documentChunkEmbeddingRepository
                        .findAllByDocumentChunk_IdInAndEmbeddingModel(chunkIds, embeddingModel)
                        .stream()
                        .collect(Collectors.toMap(
                                embedding -> embedding.getDocumentChunk().getId(),
                                Function.identity()
                        ));

        List<ChunkEmbeddingTarget> targets = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            if (chunk == null || chunk.getId() == null) {
                continue;
            }

            String embeddingText = embeddingTextBuilder.buildChunkEmbeddingText(chunk);

            if (embeddingText == null || embeddingText.isBlank()) {
                log.warn("임베딩 대상 청크 텍스트가 비어 있어 제외합니다. documentChunkId={}", chunk.getId());
                continue;
            }

            String textHash = embeddingTextHashGenerator.hash(embeddingText);
            DocumentChunkEmbedding existingEmbedding = existingEmbeddingMap.get(chunk.getId());

            if (existingEmbedding != null && existingEmbedding.hasSameTextHash(textHash)) {
                continue;
            }

            targets.add(new ChunkEmbeddingTarget(
                    chunk,
                    existingEmbedding,
                    embeddingText,
                    textHash
            ));
        }

        if (targets.isEmpty()) {
            log.info(
                    "생성 또는 갱신할 문서 청크 임베딩이 없습니다. chunkCount={}, embeddingModel={}",
                    chunks.size(),
                    embeddingModel
            );
            return;
        }

        for (int start = 0; start < targets.size(); start += EMBEDDING_BATCH_SIZE) {
            int end = Math.min(start + EMBEDDING_BATCH_SIZE, targets.size());
            List<ChunkEmbeddingTarget> batch = targets.subList(start, end);

            List<String> texts = batch.stream()
                    .map(ChunkEmbeddingTarget::embeddingText)
                    .toList();

            List<List<Double>> embeddings = embeddingClient.embedAll(texts);

            if (embeddings.size() != batch.size()) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            List<DocumentChunkEmbedding> newEmbeddings = new ArrayList<>();

            for (int i = 0; i < batch.size(); i++) {
                ChunkEmbeddingTarget target = batch.get(i);
                List<Double> embedding = embeddings.get(i);

                if (embedding == null || embedding.isEmpty()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
                }

                String embeddingJson = embeddingJsonConverter.toJson(embedding);

                if (target.existingEmbedding() == null) {
                    DocumentChunkEmbedding newEmbedding = DocumentChunkEmbedding.builder()
                            .documentChunk(target.documentChunk())
                            .embeddingModel(embeddingModel)
                            .embeddingJson(embeddingJson)
                            .embeddingDimension(embedding.size())
                            .embeddingTextHash(target.textHash())
                            .build();

                    newEmbeddings.add(newEmbedding);
                } else {
                    target.existingEmbedding().updateEmbedding(
                            embeddingJson,
                            embedding.size(),
                            target.textHash()
                    );
                }
            }

            if (!newEmbeddings.isEmpty()) {
                documentChunkEmbeddingRepository.saveAll(newEmbeddings);
            }
        }

        log.info(
                "문서 청크 임베딩 생성/갱신 완료. chunkCount={}, targetCount={}, embeddingModel={}",
                chunks.size(),
                targets.size(),
                embeddingModel
        );
    }

    @Transactional
    public void ensureScopeNodeEmbeddings(List<ExamScopeNode> scopeNodes) {
        if (scopeNodes == null || scopeNodes.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        String embeddingModel = embeddingModelProvider.getEmbeddingModel();

        List<Long> scopeNodeIds = scopeNodes.stream()
                .filter(Objects::nonNull)
                .map(ExamScopeNode::getId)
                .filter(Objects::nonNull)
                .toList();

        if (scopeNodeIds.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        Map<Long, ExamScopeNodeEmbedding> existingEmbeddingMap =
                examScopeNodeEmbeddingRepository
                        .findAllByExamScopeNode_IdInAndEmbeddingModel(scopeNodeIds, embeddingModel)
                        .stream()
                        .collect(Collectors.toMap(
                                embedding -> embedding.getExamScopeNode().getId(),
                                Function.identity()
                        ));

        List<ScopeNodeEmbeddingTarget> targets = new ArrayList<>();

        for (ExamScopeNode scopeNode : scopeNodes) {
            if (scopeNode == null || scopeNode.getId() == null) {
                continue;
            }

            String embeddingText = embeddingTextBuilder.buildScopeNodeEmbeddingText(scopeNode);

            if (embeddingText == null || embeddingText.isBlank()) {
                log.warn("임베딩 대상 목차 텍스트가 비어 있어 제외합니다. examScopeNodeId={}", scopeNode.getId());
                continue;
            }

            String textHash = embeddingTextHashGenerator.hash(embeddingText);
            ExamScopeNodeEmbedding existingEmbedding = existingEmbeddingMap.get(scopeNode.getId());

            if (existingEmbedding != null && existingEmbedding.hasSameTextHash(textHash)) {
                continue;
            }

            targets.add(new ScopeNodeEmbeddingTarget(
                    scopeNode,
                    existingEmbedding,
                    embeddingText,
                    textHash
            ));
        }

        if (targets.isEmpty()) {
            log.info(
                    "생성 또는 갱신할 시험 목차 임베딩이 없습니다. scopeNodeCount={}, embeddingModel={}",
                    scopeNodes.size(),
                    embeddingModel
            );
            return;
        }

        for (int start = 0; start < targets.size(); start += EMBEDDING_BATCH_SIZE) {
            int end = Math.min(start + EMBEDDING_BATCH_SIZE, targets.size());
            List<ScopeNodeEmbeddingTarget> batch = targets.subList(start, end);

            List<String> texts = batch.stream()
                    .map(ScopeNodeEmbeddingTarget::embeddingText)
                    .toList();

            List<List<Double>> embeddings = embeddingClient.embedAll(texts);

            if (embeddings.size() != batch.size()) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            List<ExamScopeNodeEmbedding> newEmbeddings = new ArrayList<>();

            for (int i = 0; i < batch.size(); i++) {
                ScopeNodeEmbeddingTarget target = batch.get(i);
                List<Double> embedding = embeddings.get(i);

                if (embedding == null || embedding.isEmpty()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
                }

                String embeddingJson = embeddingJsonConverter.toJson(embedding);

                if (target.existingEmbedding() == null) {
                    ExamScopeNodeEmbedding newEmbedding = ExamScopeNodeEmbedding.builder()
                            .examScopeNode(target.examScopeNode())
                            .embeddingModel(embeddingModel)
                            .embeddingJson(embeddingJson)
                            .embeddingDimension(embedding.size())
                            .embeddingTextHash(target.textHash())
                            .build();

                    newEmbeddings.add(newEmbedding);
                } else {
                    target.existingEmbedding().updateEmbedding(
                            embeddingJson,
                            embedding.size(),
                            target.textHash()
                    );
                }
            }

            if (!newEmbeddings.isEmpty()) {
                examScopeNodeEmbeddingRepository.saveAll(newEmbeddings);
            }
        }

        log.info(
                "시험 목차 임베딩 생성/갱신 완료. scopeNodeCount={}, targetCount={}, embeddingModel={}",
                scopeNodes.size(),
                targets.size(),
                embeddingModel
        );
    }

    private record ChunkEmbeddingTarget(
            DocumentChunk documentChunk,
            DocumentChunkEmbedding existingEmbedding,
            String embeddingText,
            String textHash
    ) {
    }

    private record ScopeNodeEmbeddingTarget(
            ExamScopeNode examScopeNode,
            ExamScopeNodeEmbedding existingEmbedding,
            String embeddingText,
            String textHash
    ) {
    }
}
