package com.aha.domain.ailearn.document.client.content;

import com.aha.domain.ailearn.document.service.processing.DocumentProcessingRetryExecutor;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : OpenAiLearningContentGenerationClient
 * @since : 2026. 6. 25. 목요일
 */

@ConditionalOnProperty(name = "app.ai.openai.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@Component
public class OpenAiLearningContentGenerationClient implements LearningContentGenerationClient{

    private final ChatClient chatClient;
    private final LearningContentGenerationPromptBuilder promptBuilder;
    private final DocumentProcessingRetryExecutor retryExecutor;

    public OpenAiLearningContentGenerationClient(ChatClient.Builder chatClientBuilder, LearningContentGenerationPromptBuilder promptBuilder, DocumentProcessingRetryExecutor retryExecutor) {
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.retryExecutor = retryExecutor;
    }

    @Override
    public String generate(String topicTitle, String documentContext) {

        String prompt = promptBuilder.build(topicTitle, documentContext);

        log.info(
                "문서 기반 개념 설명 생성 요청 시작. topicTitle={}, contextLength={}",
                topicTitle,
                documentContext.length()
        );

        String generatedContent;

        try{
            generatedContent = retryExecutor.execute(
                    "learning-content-generation",
                    () -> chatClient.prompt()
                            .user(prompt)
                            .call()
                            .content()
            );

        } catch (BusinessException exception){
            throw exception;

        } catch (Exception exception){

            log.error(
                    "문서 기반 개념 설명 생성 요청 실패. topicTitle={}",
                    topicTitle,
                    exception
            );

            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);

        }

        if(generatedContent == null || generatedContent.isBlank()){

            log.error(
                    "문서 기반 개념 설명 생성 결과가 비어 있습니다. topicTitle={}",
                    topicTitle
            );

            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);

        }

        String normalizedContent = generatedContent.trim();

        log.info(
                "문서 기반 개념 설명 생성 요청 완료. topicTitle={}, contentLength={}",
                topicTitle,
                normalizedContent.length()
        );

        return normalizedContent;

    }

}
