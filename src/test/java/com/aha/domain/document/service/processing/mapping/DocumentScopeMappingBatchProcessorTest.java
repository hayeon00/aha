package com.aha.domain.document.service.processing.mapping;

import com.aha.domain.document.client.mapping.DocumentScopeMappingClient;
import com.aha.domain.document.client.mapping.dto.ChunkScopeMappingRequest;
import com.aha.domain.document.client.mapping.dto.ScopeMappingAiResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentScopeMappingBatchProcessorTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @AfterEach
    void shutDownExecutor() {
        executor.shutdownNow();
    }

    @Test
    @SuppressWarnings("unchecked")
    void processesEightItemBatchesConcurrentlyAndPreservesAllResults() throws Exception {
        DocumentScopeMappingClient mappingClient = mock(DocumentScopeMappingClient.class);
        CountDownLatch allBatchesStarted = new CountDownLatch(2);
        AtomicLong resultId = new AtomicLong();

        when(mappingClient.mapChunks(anyList())).thenAnswer(invocation -> {
            allBatchesStarted.countDown();
            assertThat(allBatchesStarted.await(2, TimeUnit.SECONDS)).isTrue();
            long id = resultId.incrementAndGet();
            return List.of(new ScopeMappingAiResult(
                    id,
                    id * 10,
                    BigDecimal.ONE,
                    "mapped"
            ));
        });

        DocumentScopeMappingBatchProcessor processor =
                new DocumentScopeMappingBatchProcessor(mappingClient, executor, 8);
        List<ChunkScopeMappingRequest> requests = IntStream.range(0, 13)
                .mapToObj(index -> new ChunkScopeMappingRequest(null, List.of()))
                .toList();

        List<ScopeMappingAiResult> results = processor.process(requests);

        assertThat(results).hasSize(2);
        ArgumentCaptor<List<ChunkScopeMappingRequest>> batchCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(mappingClient, times(2)).mapChunks(batchCaptor.capture());
        assertThat(batchCaptor.getAllValues())
                .extracting(List::size)
                .containsExactlyInAnyOrder(8, 5);
    }
}
