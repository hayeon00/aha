package com.aha.domain.ailearn.document.client.mapping;

import com.aha.domain.ailearn.document.dto.mapping.request.ChunkMappingRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.request.ScopeCandidateRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.response.ScopeMappingAiResultResponseDto;
import com.aha.domain.ailearn.document.service.processing.DocumentProcessingRetryExecutor;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@ConditionalOnProperty(name = "app.ai.openai.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@Component
public class OpenAiDocumentScopeMappingClient implements DocumentScopeMappingClient {

    private final ChatClient chatClient;
    private final DocumentScopeMappingPromptBuilder promptBuilder;
    private final DocumentScopeMappingResponseParser responseParser;
    private final DocumentProcessingRetryExecutor retryExecutor;

    public OpenAiDocumentScopeMappingClient(ChatClient.Builder chatClientBuilder, DocumentScopeMappingPromptBuilder promptBuilder, DocumentScopeMappingResponseParser responseParser,DocumentProcessingRetryExecutor retryExecutor) {

        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
        this.retryExecutor = retryExecutor;

    }

    @Override
    public List<ScopeMappingAiResultResponseDto> mapChunks(List<ChunkMappingRequestDto>  chunks, List<ScopeCandidateRequestDto> scopeCandidates){

        validateInput(chunks, scopeCandidates);

        String prompt =  promptBuilder.build(chunks, scopeCandidates);

        log.info(
                "OpenAI 목차 매핑 요청 시작. chunkCount={}, scopeNodeCount={}",
                chunks.size(),
                scopeCandidates.size()
        );

        String aiResponse;

        try{
            aiResponse = retryExecutor.execute(
                    "document-scope-mapping",
                    () -> chatClient.prompt()
                            .user(prompt)
                            .call()
                            .content());

        }catch (BusinessException exception){
            throw exception;

        } catch (Exception exception){
            log.error(
                    "OpenAI 목차 매핑 요청 실패.",
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        if(aiResponse == null || aiResponse.isBlank()){

            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        List<ScopeMappingAiResultResponseDto> results = responseParser.parse(aiResponse);

        log.info(
                "OpenAI 목차 매핑 요청 완료. mappingCount={}",
                results.size()
        );

        return results;
    }

    private void validateInput(List<ChunkMappingRequestDto> chunks, List<ScopeCandidateRequestDto> scopeCandidates) {

        if(chunks == null || chunks.isEmpty()){

            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        if(scopeCandidates == null || scopeCandidates.isEmpty()){

            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }
    }


}