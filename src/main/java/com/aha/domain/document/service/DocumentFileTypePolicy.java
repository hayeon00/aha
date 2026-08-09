package com.aha.domain.document.service;

import java.util.Map;
import java.util.Set;

public final class DocumentFileTypePolicy {

    private static final Map<String, Set<String>>
            ALLOWED_MIME_TYPES_BY_EXTENSION =
            Map.of(
                    "pdf",
                    Set.of(
                            "application/pdf"
                    ),
                    "docx",
                    Set.of(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
            );

    private DocumentFileTypePolicy() {
    }

    public static boolean supportsExtension(
            String extension
    ) {
        return extension != null
                && ALLOWED_MIME_TYPES_BY_EXTENSION
                .containsKey(extension);
    }

    public static Set<String> getAllowedMimeTypes(
            String extension
    ) {
        return ALLOWED_MIME_TYPES_BY_EXTENSION
                .get(extension);
    }
}