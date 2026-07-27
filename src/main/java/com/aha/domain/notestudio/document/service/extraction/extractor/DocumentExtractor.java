package com.aha.domain.notestudio.document.service.extraction.extractor;

import com.aha.domain.notestudio.document.entity.SourceDocument;
import com.aha.domain.notestudio.document.service.extraction.model.ExtractedDocument;

import java.nio.file.Path;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentExtractor
 * @since : 2026. 7. 8. 수요일
 */
public interface DocumentExtractor {

    boolean supports(String extension);

    ExtractedDocument extract(SourceDocument sourceDocument, Path documentPath);
}