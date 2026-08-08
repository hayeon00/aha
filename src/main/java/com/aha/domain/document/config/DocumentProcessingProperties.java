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
    private long timeoutSeconds = 60;
    private int retryMaxAttempts = 3;
    private long retryBackoffMillis = 1000;


}
