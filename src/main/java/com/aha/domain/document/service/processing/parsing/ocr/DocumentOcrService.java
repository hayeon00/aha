package com.aha.domain.document.service.processing.parsing.ocr;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Slf4j
@Service
public class DocumentOcrService {

    private final ITesseract tesseract;

    public DocumentOcrService(
            @Value("${document.ocr.data-path:}") String configuredDataPath,
            @Value("${document.ocr.native-library-path:}") String configuredNativeLibraryPath,
            @Value("${document.ocr.language:kor+eng}") String language
    ) {
        String dataPath = resolveDataPath(configuredDataPath);
        String nativeLibraryPath = resolveNativeLibraryPath(configuredNativeLibraryPath);

        if (nativeLibraryPath != null) {
            System.setProperty("jna.library.path", nativeLibraryPath);
        }

        Tesseract instance = new Tesseract();

        if (dataPath != null) {
            instance.setDatapath(dataPath);
        } else {
            log.warn(
                    "Tesseract 학습 데이터 경로를 찾지 못했습니다. "
                            + "OCR 사용 전 TESSERACT_DATA_PATH를 설정해 주세요."
            );
        }
        instance.setLanguage(language);

        this.tesseract = instance;

        log.info(
                "Tesseract OCR 설정 완료. dataPath={}, nativeLibraryPath={}, language={}",
                dataPath == null ? "미설정" : dataPath,
                nativeLibraryPath == null ? "운영체제 기본 검색 경로" : nativeLibraryPath,
                language
        );
    }

    public String extractText(
            BufferedImage image
    ) {
        if (image == null) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }

        try {
            String text =
                    tesseract.doOCR(
                            image
                    );

            if (text == null
                    || text.isBlank()) {

                return null;
            }

            return normalize(
                    text
            );

        } catch (UnsatisfiedLinkError error) {
            log.error(
                    "Tesseract 네이티브 라이브러리를 불러오지 못했습니다. "
                            + "TESSERACT_NATIVE_LIBRARY_PATH 설정을 확인해 주세요.",
                    error
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );

        } catch (Exception exception) {

            log.error(
                    "OCR 텍스트 추출 실패",
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }
    }

    private String normalize(
            String text
    ) {
        if (text == null
                || text.isBlank()) {
            return null;
        }

        String normalized =
                text.replace("\r\n", "\n")
                        .replace('\r', '\n')
                        .replace('\u00A0', ' ')
                        .replaceAll("[\\t ]+", " ")
                        .replaceAll("\\n{3,}", "\n\n")
                        .trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }

    private String resolveDataPath(String configuredPath) {
        String resolved = resolveExistingDirectory(configuredPath, List.of());

        if (resolved == null) {
            resolved = defaultDataPathCandidates().stream()
                    .filter(this::containsRequiredLanguageData)
                    .findFirst()
                    .map(Path::toString)
                    .orElse(null);
        }

        if (resolved == null) return null;

        if (!containsRequiredLanguageData(Path.of(resolved))) {
            throw new IllegalStateException(
                    "Tesseract 학습 데이터에 kor.traineddata 또는 eng.traineddata가 없습니다: "
                            + resolved
            );
        }

        return resolved;
    }

    private boolean containsRequiredLanguageData(Path directory) {
        return Files.isRegularFile(directory.resolve("kor.traineddata"))
                && Files.isRegularFile(directory.resolve("eng.traineddata"));
    }

    private String resolveNativeLibraryPath(String configuredPath) {
        if (configuredPath != null && !configuredPath.isBlank()) {
            return resolveExistingDirectory(configuredPath, List.of());
        }

        return defaultNativeLibraryPathCandidates().stream()
                .filter(this::containsTesseractNativeLibrary)
                .findFirst()
                .map(Path::toString)
                .orElse(null);
    }

    private String resolveExistingDirectory(
            String configuredPath,
            List<Path> defaultCandidates
    ) {
        if (configuredPath != null && !configuredPath.isBlank()) {
            Path path = Path.of(configuredPath).toAbsolutePath().normalize();

            if (!Files.isDirectory(path)) {
                throw new IllegalStateException(
                        "설정된 Tesseract 경로가 디렉터리가 아닙니다: " + path
                );
            }

            return path.toString();
        }

        return defaultCandidates.stream()
                .filter(Files::isDirectory)
                .findFirst()
                .map(Path::toString)
                .orElse(null);
    }

    private List<Path> defaultDataPathCandidates() {
        String programFiles = System.getenv("ProgramFiles");

        return List.of(
                windowsPath(programFiles, "Tesseract-OCR", "tessdata"),
                Path.of("/opt/local/share/tessdata"),
                Path.of("/opt/homebrew/share/tessdata"),
                Path.of("/usr/local/share/tessdata"),
                Path.of("/usr/share/tesseract-ocr/5/tessdata"),
                Path.of("/usr/share/tesseract-ocr/4.00/tessdata"),
                Path.of("/usr/share/tessdata")
        );
    }

    private List<Path> defaultNativeLibraryPathCandidates() {
        String programFiles = System.getenv("ProgramFiles");

        return List.of(
                windowsPath(programFiles, "Tesseract-OCR"),
                Path.of("/opt/local/lib"),
                Path.of("/opt/homebrew/lib"),
                Path.of("/usr/local/lib")
        );
    }

    private boolean containsTesseractNativeLibrary(Path directory) {
        if (!Files.isDirectory(directory)) {
            return false;
        }

        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .anyMatch(fileName ->
                            fileName.contains("tesseract")
                                    && (fileName.endsWith(".dll")
                                    || fileName.endsWith(".dylib")
                                    || fileName.contains(".so"))
                    );
        } catch (Exception exception) {
            log.debug(
                    "Tesseract 네이티브 라이브러리 디렉터리를 확인하지 못했습니다. path={}",
                    directory,
                    exception
            );
            return false;
        }
    }

    private Path windowsPath(String basePath, String... children) {
        Path path = basePath == null || basePath.isBlank()
                ? Path.of("C:\\Program Files")
                : Path.of(basePath);

        for (String child : children) {
            path = path.resolve(child);
        }

        return path;
    }
}
