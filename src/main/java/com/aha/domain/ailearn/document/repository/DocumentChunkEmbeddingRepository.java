package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentChunkEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentChunkEmbeddingRepository extends JpaRepository<DocumentChunkEmbedding, Long> {

    // uk로 연결된 청크 조회
    Optional<DocumentChunkEmbedding> findByDocumentChunk_IdAndEmbeddingModel(
            Long documentChunkId,
            String embeddingModel
    );

    // 여러 청크 Id에 대한 임베딩을 한번에 조회 -> 유사도 계산시 사용
    List<DocumentChunkEmbedding> findAllByDocumentChunk_IdInAndEmbeddingModel(
            Collection<Long> documentChunkIds,
            String embeddingModel
    );

    // 현재 텍스트 기준 임베딩이 존재하는지 조회
    boolean existsByDocumentChunk_IdAndEmbeddingModelAndEmbeddingTextHash(
            Long documentChunkId,
            String embeddingModel,
            String embeddingTextHash
    );

    // 청크ID에 연결된 임베딩 삭제
    void deleteAllByDocumentChunk_IdIn(Collection<Long> documentChunkIds);
}