package com.aha.domain.notestudio.document.client.content;

import com.aha.domain.notestudio.document.service.content.model.GeneratedLearningContent;
import com.aha.domain.notestudio.document.service.content.model.TopicDocumentContext;

public interface LearningContentGenerationClient {

    GeneratedLearningContent generate(
            TopicDocumentContext context
    );
}