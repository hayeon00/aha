package com.aha.domain.document.enums;

import lombok.Getter;

import java.util.Locale;
import java.util.Set;

@Getter
public enum DocumentFileExtension {

    PDF(
            "pdf",
            Set.of(
                    "application/pdf"
            )
    ),

    DOCX(
            "docx",
            Set.of(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            )
    );

    private final String value;
    private final Set<String> allowedMimeTypes;

    DocumentFileExtension(
            String value,
            Set<String> allowedMimeTypes
    ) {
        this.value = value;
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public boolean supportsMimeType(
            String mimeType
    ) {
        return mimeType != null
                && allowedMimeTypes.contains(
                mimeType
                        .trim()
                        .toLowerCase(Locale.ROOT)
        );
    }

    public static DocumentFileExtension from(
            String value
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "파일 확장자는 필수입니다."
            );
        }

        String normalized =
                value.trim()
                        .toLowerCase(Locale.ROOT);

        for (DocumentFileExtension extension : values()) {
            if (extension.value.equals(normalized)) {
                return extension;
            }
        }

        throw new IllegalArgumentException(
                "지원하지 않는 파일 확장자입니다: " + value
        );
    }
}