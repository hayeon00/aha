package com.aha.domain.ailearn.document.client;

import com.aha.domain.ailearn.document.dto.generation.request.TopicLearningContentSourceDto;
import com.aha.domain.ailearn.document.dto.generation.response.LearningContentGenerationResultDto;

public interface LearningContentGenerationClient {

    LearningContentGenerationResultDto generate(
            TopicLearningContentSourceDto source
    );
}