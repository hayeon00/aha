package com.aha.domain.ailearn.document.client.mapping;

import com.aha.domain.ailearn.document.dto.mapping.request.ChunkMappingRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.request.ScopeCandidateRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.response.ScopeMappingAiResultResponseDto;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OpenAiDocumentScopeMappingClient implements DocumentScopeMappingClient {

    private final ChatClient chatClient;
    private final DocumentScopeMappingPromptBuilder promptBuilder;
    private final DocumentScopeMappingResponseParser responseParser;

    public OpenAiDocumentScopeMappingClient(ChatClient.Builder chatClientBuilder, DocumentScopeMappingPromptBuilder promptBuilder, DocumentScopeMappingResponseParser responseParser) {

        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;

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
            aiResponse = chatClient.prompt()
                                    .user(prompt)
                                    .call()
                                    .content();

        }catch (Exception exception){
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