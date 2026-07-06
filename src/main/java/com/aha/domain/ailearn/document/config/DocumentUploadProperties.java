package com.aha.domain.ailearn.document.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentUploadProperties
 * @since : 2026. 7. 6. 월요일
 */

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "file.document")
public class DocumentUploadProperties {

    private DataSize maxFileSize = DataSize.ofMegabytes(20);
    private DataSize maxTotalSize = DataSize.ofMegabytes(100);
    private int maxFileCount = 5;
    private int maxFileNameLength = 255;

    public long getMaxFileSizeBytes() {
        return maxFileSize.toBytes();
    }

    public long getMaxTotalSizeBytes() {
        return maxTotalSize.toBytes();
    }
}