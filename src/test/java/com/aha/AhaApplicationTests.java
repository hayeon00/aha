package com.aha;

import com.aha.domain.notestudio.document.client.mapping.OpenAiDocumentScopeMappingClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
class AhaApplicationTests {

    @MockitoBean
    private OpenAiDocumentScopeMappingClient documentScopeMappingClient;

    @Test
    void contextLoads() {
    }

}
