package com.aha.domain.notestudio.document.service.content;

import com.aha.domain.notestudio.document.client.content.LearningContentGenerationClient;
import com.aha.domain.notestudio.document.service.content.model.GeneratedLearningContent;
import com.aha.domain.notestudio.document.service.content.model.TopicDocumentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LearningContentGenerationService {

    private final LearningContentGenerationClient
            learningContentGenerationClient;

    public GeneratedLearningContent generate(
            TopicDocumentContext context
    ) {
        return learningContentGenerationClient
                .generate(context);
    }
}