package com.aha.domain.document.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "file.document")
public class DocumentUploadProperties {

    private DataSize maxFileSize = DataSize.ofMegabytes(20);
    private DataSize maxTotalSize = DataSize.ofMegabytes(100);
    private int maxFileCount = 5;
    private int maxFileNameLength = 255;
    private Duration cleanupStaleAfter = Duration.ofHours(1);
    private long cleanupIntervalMs = 600_000L;

    public long getMaxFileSizeBytes() {
        return maxFileSize.toBytes();
    }

    public long getMaxTotalSizeBytes() {
        return maxTotalSize.toBytes();
    }
}
