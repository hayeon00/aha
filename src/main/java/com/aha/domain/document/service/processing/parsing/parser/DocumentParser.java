package com.aha.domain.document.service.processing.parsing.parser;

import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentFileExtension;
import com.aha.domain.document.service.processing.parsing.model.ParsedDocument;

import java.nio.file.Path;

public interface DocumentParser {

    boolean supports(
            DocumentFileExtension fileExtension
    );

    ParsedDocument parse(
            SourceDocument sourceDocument,
            Path documentPath
    );
}