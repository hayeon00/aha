package com.aha.domain.ailearn.document.service.upload;

import java.util.Map;
import java.util.Set;

final class DocumentFileTypePolicy {

    static final Map<String, Set<String>> ALLOWED_MIME_TYPES =
            Map.of(
                    "pdf", Set.of(
                            "application/pdf"
                    ),
                    "docx", Set.of(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
            );

    private DocumentFileTypePolicy() {
    }

    static Set<String> getAllowedMimeTypes(String extension) {
        return ALLOWED_MIME_TYPES.get(extension);
    }

    static boolean supportsExtension(String extension) {
        return ALLOWED_MIME_TYPES.containsKey(extension);
    }
}