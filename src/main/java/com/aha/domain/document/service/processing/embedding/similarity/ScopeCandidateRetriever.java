package com.aha.domain.document.service.processing.embedding.similarity;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.service.processing.embedding.similarity.model.ScopeCandidateSearchResult;
import com.aha.domain.exam.entity.ExamScopeNode;

import java.util.List;
import java.util.Map;

/**
 * 문서 Chunk와 시험 Topic 간 후보 검색 전략.
 *
 * 현재는 In-Memory cosine similarity를 사용하고,
 * 향후 pgvector / OpenSearch / Vector DB 기반 구현체로 교체할 수 있다.
 */
public interface ScopeCandidateRetriever {

    Map<Long, List<ScopeCandidateSearchResult>> retrieve(
            Long examVersionId,
            List<DocumentChunk> chunks,
            List<ExamScopeNode> scopeNodes,
            int topN
    );
}