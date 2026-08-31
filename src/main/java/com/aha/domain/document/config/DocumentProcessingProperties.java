package com.aha.domain.document.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "document.processing")
public class DocumentProcessingProperties {

    private int corePoolSize = 2;
    private int maxPoolSize = 4;
    private int queueCapacity = 20;
    private int keepAliveSeconds = 60;
    private int awaitTerminationSeconds = 30;
    private int contentGenerationConcurrency = 3;
    private int contentGenerationQueueCapacity = 100;
    private int scopeMappingConcurrency = 2;
    private int scopeMappingQueueCapacity = 50;
    private int scopeMappingBatchSize = 8;
    private boolean semanticFastPathEnabled = true;
    private double semanticFastPathMinSimilarity = 0.92;
    private double semanticFastPathMinMargin = 0.20;
    private int contentGenerationMaxSourceChunks = 8;
    private int contentGenerationMaxSourceCharacters = 12000;
    private int contentGenerationBatchSize = 1;
    private int contentGenerationMaxOutputTokens = 1000;
    private long timeoutSeconds = 60;
    private int retryMaxAttempts = 3;
    private long retryBackoffMillis = 1000;


}
