package com.aha.domain.document.enums;

import java.util.Locale;

public enum DocumentFileExtension {

    PDF,
    DOCX;

    public static DocumentFileExtension from(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("파일 확장자는 필수입니다.");
        }

        try {
            return DocumentFileExtension.valueOf(
                    extension.trim()
                            .replace(".", "")
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "지원하지 않는 파일 확장자입니다: " + extension
            );
        }
    }
}