package com.aha.domain.ailearn.document.service.extraction.classifier.detector;

import com.aha.domain.ailearn.document.service.extraction.classifier.ClassifiedBlockType;
import com.aha.domain.ailearn.document.service.extraction.classifier.DocumentBlockTypeDetector;
import org.springframework.stereotype.Component;

@Component
public class SqlCodeBlockDetector implements DocumentBlockTypeDetector {

    @Override
    public boolean supports(String text) {
        return looksLikeSqlCode(text);
    }

    @Override
    public ClassifiedBlockType detect(String text) {
        return ClassifiedBlockType.sqlCode();
    }

    @Override
    public int order() {
        return 30;
    }

    private boolean looksLikeSqlCode(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalizedText = text.trim();

        if (normalizedText.length() < 6) {
            return false;
        }

        String upperText = normalizedText.toUpperCase();

        // 한글 설명 문장으로 보이면 SQL 코드가 아니라 TEXT로 본다.
        if (looksLikeNaturalLanguageExplanation(upperText)) {
            return false;
        }

        // 여러 줄 SQL 코드
        if (looksLikeMultiLineSqlCode(upperText)) {
            return true;
        }

        // 단일 SQL 문장
        if (looksLikeSingleSqlStatement(upperText)) {
            return true;
        }

        // 복잡한 SQL 패턴
        return looksLikeComplexSqlExpression(upperText);
    }

    private boolean looksLikeNaturalLanguageExplanation(String upperText) {
        return upperText.contains(" 문은")
                || upperText.contains(" 문장")
                || upperText.contains(" 절은")
                || upperText.contains(" 구문은")
                || upperText.contains(" 키워드")
                || upperText.contains(" 명령어이다")
                || upperText.contains(" 명령어")
                || upperText.contains(" 설명")
                || upperText.contains(" 설명하는")
                || upperText.contains(" 일반 텍스트")
                || upperText.contains(" 코드가 아니라")
                || upperText.contains(" 사용하는")
                || upperText.contains(" 사용한다")
                || upperText.contains(" 의미한다")
                || upperText.contains(" 조회할 때")
                || upperText.contains(" 조건을")
                || upperText.contains(" 그룹화")
                || upperText.contains(" 정렬")
                || upperText.contains(" 연결")
                || upperText.contains(" 데이터베이스")
                || upperText.contains(" 데이터를")
                || upperText.contains(" 테이블")
                || upperText.contains(" 컬럼");
    }

    private boolean looksLikeSingleSqlStatement(String upperText) {
        String normalized = upperText.replaceAll("\\s+", " ").trim();

        if (normalized.matches("^SELECT\\s+.+\\s+FROM\\s+.+")) {
            return true;
        }

        if (normalized.matches("^WITH\\s+.+\\s+AS\\s*\\(.+\\)\\s*SELECT\\s+.+")) {
            return true;
        }

        if (normalized.matches("^INSERT\\s+INTO\\s+.+\\s+VALUES\\s*\\(.+\\).*")) {
            return true;
        }

        if (normalized.matches("^UPDATE\\s+.+\\s+SET\\s+.+")) {
            return true;
        }

        if (normalized.matches("^DELETE\\s+FROM\\s+.+")) {
            return true;
        }

        if (normalized.matches("^CREATE\\s+TABLE\\s+.+")) {
            return true;
        }

        if (normalized.matches("^(ALTER|DROP|TRUNCATE)\\s+TABLE\\s+.+")) {
            return true;
        }

        if (normalized.matches("^(COMMIT|ROLLBACK)\\s*;?$")) {
            return true;
        }

        return false;
    }

    private boolean looksLikeComplexSqlExpression(String upperText) {
        return upperText.matches("(?s).*\\bSELECT\\b.*\\bFROM\\b.*\\bWHERE\\b.*")
                || upperText.matches("(?s).*\\bEXISTS\\s*\\(\\s*SELECT\\b.*")
                || upperText.matches("(?s).*\\bIN\\s*\\(\\s*SELECT\\b.*")
                || upperText.matches("(?s).*\\bOVER\\s*\\(.*\\bPARTITION\\s+BY\\b.*")
                || upperText.matches("(?s).*\\bCASE\\b.*\\bWHEN\\b.*\\bTHEN\\b.*\\bEND\\b.*")
                || upperText.matches("(?s).*\\bUNION\\b.*\\bSELECT\\b.*");
    }



    private boolean looksLikeMultiLineSqlCode(String upperText) {
        String[] lines = upperText.split("\\n");

        if (lines.length < 2) {
            return false;
        }

        int sqlLineCount = 0;

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.matches(
                    "^(SELECT|FROM|WHERE|GROUP\\s+BY|HAVING|ORDER\\s+BY|JOIN|INNER\\s+JOIN|LEFT\\s+JOIN|RIGHT\\s+JOIN|FULL\\s+OUTER\\s+JOIN|ON|WITH|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP|VALUES|SET|UNION|CASE|WHEN|ELSE|END)\\b.*"
            )) {
                sqlLineCount++;
            }
        }

        return sqlLineCount >= 2;
    }
}