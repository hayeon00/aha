package com.aha.domain.ailearn.document.service.extraction;

import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class DocumentTextExtractionService {

    @Value("${file.document-upload-dir:uploads/documents}")
    private String documentUploadDir;

    /**
     * DocumentProcessing에 연결된 실제 파일을 읽어서
     * 문서 본문 텍스트를 반환한다.
     */
    public String extract(DocumentProcessing processing) {
        if (processing == null
                || processing.getSourceDocument() == null) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_PROCESSING_NOT_FOUND
            );
        }

        SourceDocument sourceDocument =
                processing.getSourceDocument();

        Path documentPath =
                resolveDocumentPath(sourceDocument);

        validateDocumentPath(documentPath);

        try (InputStream inputStream =
                     Files.newInputStream(documentPath)) {

            AutoDetectParser parser =
                    new AutoDetectParser();

            /*
             * -1을 전달하면 추출할 문자 수를 제한하지 않는다.
             */
            BodyContentHandler handler =
                    new BodyContentHandler(-1);

            Metadata metadata =
                    new Metadata();

            ParseContext parseContext =
                    new ParseContext();

            parser.parse(
                    inputStream,
                    handler,
                    metadata,
                    parseContext
            );

            String extractedText =
                    normalizeText(handler.toString());

            if (extractedText.isBlank()) {
                throw new BusinessException(
                        ErrorCode.DOCUMENT_TEXT_EMPTY
                );
            }

            log.info(
                    "문서 텍스트 추출 완료. processingId={}, sourceDocumentId={}, fileName={}, textLength={}",
                    processing.getId(),
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName(),
                    extractedText.length()
            );

            return extractedText;

        } catch (BusinessException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "문서 텍스트 추출 실패. processingId={}, sourceDocumentId={}, fileName={}, path={}",
                    processing.getId(),
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName(),
                    documentPath,
                    e
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }
    }

    /**
     * 업로드 루트 경로와 SourceDocument의 storageKey를 조합한다.
     *
     * 예:
     * documentUploadDir = uploads/documents
     * storageKey = 1/3/10/uuid.pdf
     *
     * 최종 경로:
     * uploads/documents/1/3/10/uuid.pdf
     */
    private Path resolveDocumentPath(
            SourceDocument sourceDocument
    ) {
        String storageKey =
                sourceDocument.getStorageKey();

        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }

        Path uploadRoot =
                Paths.get(documentUploadDir)
                        .toAbsolutePath()
                        .normalize();

        Path resolvedPath =
                uploadRoot
                        .resolve(storageKey)
                        .normalize();

        /*
         * storageKey에 ../ 등이 포함되어
         * 업로드 루트 밖으로 이동하는 것을 방지한다.
         */
        if (!resolvedPath.startsWith(uploadRoot)) {
            log.warn(
                    "허용되지 않은 문서 경로입니다. sourceDocumentId={}, storageKey={}",
                    sourceDocument.getId(),
                    storageKey
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }

        return resolvedPath;
    }

    /**
     * 실제 파일이 존재하고 읽을 수 있는지 검사한다.
     */
    private void validateDocumentPath(
            Path documentPath
    ) {
        if (!Files.exists(documentPath)
                || !Files.isRegularFile(documentPath)
                || !Files.isReadable(documentPath)) {

            log.warn(
                    "문서 파일을 읽을 수 없습니다. path={}",
                    documentPath
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }
    }

    /**
     * Tika가 추출한 텍스트를 청킹하기 좋은 형태로 정리한다.
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        return text
                /*
                 * 파일 파싱 중 포함될 수 있는 NULL 문자 제거
                 */
                .replace("\u0000", "")

                /*
                 * 운영체제별 줄바꿈을 \n으로 통일
                 */
                .replace("\r\n", "\n")
                .replace("\r", "\n")

                /*
                 * 연속된 탭과 공백을 하나의 공백으로 정리
                 */
                .replaceAll("[\\t ]+", " ")

                /*
                 * 세 줄 이상의 연속된 줄바꿈을 두 줄로 축소
                 */
                .replaceAll("\\n{3,}", "\n\n")

                .trim();
    }
}