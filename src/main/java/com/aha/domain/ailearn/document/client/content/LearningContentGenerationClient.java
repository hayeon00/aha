package com.aha.domain.ailearn.document.client.content;

import com.aha.domain.ailearn.document.service.content.model.GeneratedLearningContent;
import com.aha.domain.ailearn.document.service.content.model.TopicDocumentContext;

public interface LearningContentGenerationClient {

    GeneratedLearningContent generate(
            TopicDocumentContext context
    );
}