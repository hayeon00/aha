package com.aha.domain.document.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "document.upload")
public class DocumentUploadProperties {

    private String directory = "uploads/documents";

    private DataSize maxFileSize = DataSize.ofMegabytes(20);

    private int maxFileNameLength = 255;

    public long getMaxFileSizeBytes() {
        return maxFileSize.toBytes();
    }
}