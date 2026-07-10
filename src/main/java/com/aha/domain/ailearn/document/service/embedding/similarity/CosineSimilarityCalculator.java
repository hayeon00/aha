package com.aha.domain.ailearn.document.service.embedding.similarity;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CosineSimilarityCalculator {

    public double calculate(List<Double> vectorA, List<Double> vectorB) {
        validateVectors(vectorA, vectorB);

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            double valueA = vectorA.get(i);
            double valueB = vectorB.get(i);

            dotProduct += valueA * valueB;
            normA += valueA * valueA;
            normB += valueB * valueB;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private void validateVectors(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA == null || vectorA.isEmpty() || vectorB == null || vectorB.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (vectorA.size() != vectorB.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}