package com.aha.domain.learningnote.service.generation;

import com.aha.domain.document.enums.DocumentChunkContentType;
import com.aha.domain.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.document.repository.projection.MappedTopicChunkProjection;
import com.aha.domain.learningnote.client.generation.ConceptExplanationClient;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningNoteContentGenerationServiceTest {

    @Mock
    private DocumentScopeMappingRepository mappingRepository;
    @Mock
    private ConceptExplanationClient explanationClient;
    @Mock
    private LearningNoteContentPersistenceService persistenceService;

    private LearningNoteContentGenerationService service;

    @BeforeEach
    void setUp() {
        service = new LearningNoteContentGenerationService(
                mappingRepository,
                explanationClient,
                persistenceService
        );
    }

    @Test
    void 매핑된_청크를_목차별로_묶어_설명을_생성하고_저장한다() {
        List<MappedTopicChunkProjection> mappedChunks = List.of(
                projection(100L, "데이터 모델링", 1L, 1, "첫 번째 내용"),
                projection(100L, "데이터 모델링", 2L, 2, "두 번째 내용"),
                projection(200L, "SQL 기본", 3L, 3, "세 번째 내용")
        );
        when(mappingRepository.findMappedTopicChunks(10L)).thenReturn(mappedChunks);
        when(explanationClient.generate(any())).thenAnswer(invocation -> {
            ConceptExplanationRequest request = invocation.getArgument(0);
            return new ConceptExplanationResult(
                    request.topicTitle(),
                    request.topicTitle() + " 설명"
            );
        });

        service.generate(10L);

        ArgumentCaptor<ConceptExplanationRequest> requestCaptor =
                ArgumentCaptor.forClass(ConceptExplanationRequest.class);
        verify(explanationClient, times(2)).generate(requestCaptor.capture());

        List<ConceptExplanationRequest> requests = requestCaptor.getAllValues();
        assertThat(requests.get(0).examScopeNodeId()).isEqualTo(100L);
        assertThat(requests.get(0).sourceChunks()).hasSize(2);
        assertThat(requests.get(1).examScopeNodeId()).isEqualTo(200L);
        assertThat(requests.get(1).sourceChunks()).hasSize(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GeneratedConceptExplanation>> resultCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(persistenceService).replaceDocumentBasedContents(eq(10L), resultCaptor.capture());
        assertThat(resultCaptor.getValue()).hasSize(2);
    }

    @Test
    void 목차_매핑이_없으면_생성을_시작하지_않는다() {
        when(mappingRepository.findMappedTopicChunks(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.generate(10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.DOCUMENT_SCOPE_MAPPING_NOT_FOUND));

        verifyNoInteractions(explanationClient, persistenceService);
    }

    @Test
    void 선택한_목차의_청크만_사용해_개념설명을_추가_생성한다() {
        MappedTopicChunkProjection otherTopic = mock(MappedTopicChunkProjection.class);
        when(otherTopic.getTopicId()).thenReturn(200L);
        List<MappedTopicChunkProjection> mappedChunks = List.of(
                projection(100L, "데이터 모델링", 1L, 1, "선택 목차 내용"),
                otherTopic
        );
        when(mappingRepository.findMappedTopicChunks(10L)).thenReturn(mappedChunks);
        when(explanationClient.generate(any())).thenReturn(
                new ConceptExplanationResult("데이터 모델링", "생성된 설명")
        );

        service.generateTopic(10L, 100L);

        ArgumentCaptor<ConceptExplanationRequest> requestCaptor =
                ArgumentCaptor.forClass(ConceptExplanationRequest.class);
        verify(explanationClient).generate(requestCaptor.capture());
        assertThat(requestCaptor.getValue().examScopeNodeId()).isEqualTo(100L);
        assertThat(requestCaptor.getValue().sourceChunks())
                .extracting(chunk -> chunk.chunkId())
                .containsExactly(1L);
        verify(persistenceService).saveDocumentBasedContent(
                eq(10L),
                any(GeneratedConceptExplanation.class)
        );
    }

    private MappedTopicChunkProjection projection(
            Long topicId,
            String topicTitle,
            Long chunkId,
            Integer chunkOrder,
            String chunkText
    ) {
        MappedTopicChunkProjection projection = mock(MappedTopicChunkProjection.class);
        when(projection.getTopicId()).thenReturn(topicId);
        when(projection.getTopicTitle()).thenReturn(topicTitle);
        when(projection.getChunkId()).thenReturn(chunkId);
        when(projection.getChunkOrder()).thenReturn(chunkOrder);
        when(projection.getChunkText()).thenReturn(chunkText);
        when(projection.getChunkType()).thenReturn(DocumentChunkContentType.TEXT);
        return projection;
    }
}
