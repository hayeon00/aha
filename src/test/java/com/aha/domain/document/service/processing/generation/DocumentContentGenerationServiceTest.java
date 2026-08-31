package com.aha.domain.document.service.processing.generation;

import com.aha.domain.document.service.processing.generation.model.TopicContentSource;
import com.aha.domain.document.service.processing.generation.model.TopicSourceChunk;
import com.aha.domain.document.service.processing.generation.model.persistence.DocumentContentGenerationPersistenceService;
import com.aha.domain.document.config.DocumentProcessingProperties;
import com.aha.domain.learningnote.client.generation.ConceptExplanationClient;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentContentGenerationServiceTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @AfterEach
    void shutDownExecutor() {
        executor.shutdownNow();
    }

    @Test
    void generatesTopicsConcurrentlyAndWaitsForEveryResult() throws Exception {
        DocumentContentGenerationSourceService sourceService =
                mock(DocumentContentGenerationSourceService.class);
        ConceptExplanationClient generationClient =
                mock(ConceptExplanationClient.class);
        DocumentContentGenerationPersistenceService persistenceService =
                mock(DocumentContentGenerationPersistenceService.class);

        List<TopicContentSource> sources = List.of(
                topicSource(1L),
                topicSource(2L),
                topicSource(3L),
                topicSource(4L)
        );
        when(sourceService.getTopicSources(10L)).thenReturn(sources);

        CountDownLatch allRequestsStarted = new CountDownLatch(4);
        when(generationClient.generate(any())).thenAnswer(invocation -> {
            allRequestsStarted.countDown();
            assertThat(allRequestsStarted.await(2, TimeUnit.SECONDS)).isTrue();
            return new ConceptExplanationResult("title", "content");
        });

        DocumentProcessingProperties properties = new DocumentProcessingProperties();
        properties.setContentGenerationBatchSize(1);
        properties.setContentGenerationConcurrency(4);

        DocumentContentGenerationService service =
                new DocumentContentGenerationService(
                        sourceService,
                        generationClient,
                        persistenceService,
                        executor,
                        properties
                );

        int generatedCount = service.generate(10L);

        assertThat(generatedCount).isEqualTo(sources.size());
        verify(generationClient, times(4)).generate(any());
        for (TopicContentSource source : sources) {
            verify(persistenceService).saveGeneratedContent(
                    eq(10L),
                    eq(source),
                    any(ConceptExplanationResult.class)
            );
        }
    }

    @Test
    void retriesOnlyFailedTopicSeriallyAfterParallelGeneration() {
        DocumentContentGenerationSourceService sourceService =
                mock(DocumentContentGenerationSourceService.class);
        ConceptExplanationClient generationClient =
                mock(ConceptExplanationClient.class);
        DocumentContentGenerationPersistenceService persistenceService =
                mock(DocumentContentGenerationPersistenceService.class);

        TopicContentSource failedSource = topicSource(1L);
        when(sourceService.getTopicSources(10L)).thenReturn(List.of(failedSource));
        when(generationClient.generate(any()))
                .thenThrow(new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED))
                .thenReturn(new ConceptExplanationResult("title", "content"));

        DocumentProcessingProperties properties = new DocumentProcessingProperties();
        properties.setContentGenerationBatchSize(1);
        properties.setContentGenerationConcurrency(3);

        DocumentContentGenerationService service =
                new DocumentContentGenerationService(
                        sourceService,
                        generationClient,
                        persistenceService,
                        executor,
                        properties
                );

        assertThat(service.generate(10L)).isEqualTo(1);
        verify(generationClient, times(2)).generate(any());
        verify(persistenceService).saveGeneratedContent(
                eq(10L),
                eq(failedSource),
                any(ConceptExplanationResult.class)
        );
    }

    private TopicContentSource topicSource(Long scopeNodeId) {
        return new TopicContentSource(
                scopeNodeId,
                "SCOPE-" + scopeNodeId,
                "Topic " + scopeNodeId,
                List.of(new TopicSourceChunk(
                        scopeNodeId * 10,
                        scopeNodeId.intValue(),
                        "Section " + scopeNodeId,
                        "TEXT",
                        "Content " + scopeNodeId
                ))
        );
    }
}
