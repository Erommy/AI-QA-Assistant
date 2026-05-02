package com.example.aitestgenerator.service;

import com.example.aitestgenerator.model.TestCaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class OpenAiServiceTest {

    private OpenAiService openAiService;

    @BeforeEach
    void setUp() {
        // Only testing parseResponse — no real HTTP calls are made
        openAiService = new OpenAiService(
                "test-key",
                "https://api.openai.com",
                "gpt-4o",
                new ObjectMapper()
        );
    }

    @Test
    void parseResponse_mapsAllNineFields() {
        String raw = buildRawOpenAiResponse("""
                {
                  "summary": "A login feature.",
                  "testCases": ["TC1", "TC2"],
                  "edgeCases": ["EC1"],
                  "negativeTests": ["NT1"],
                  "securityTests": ["ST1"],
                  "apiTests": ["AT1", "AT2"],
                  "automationSkeleton": "@Test\\npublic void test() {}",
                  "playwrightSkeleton": "test('login', async () => {});",
                  "riskLevel": "High"
                }
                """);

        TestCaseResponse result = openAiService.parseResponse(raw);

        assertThat(result.getSummary()).isEqualTo("A login feature.");
        assertThat(result.getTestCases()).containsExactly("TC1", "TC2");
        assertThat(result.getEdgeCases()).containsExactly("EC1");
        assertThat(result.getNegativeTests()).containsExactly("NT1");
        assertThat(result.getSecurityTests()).containsExactly("ST1");
        assertThat(result.getApiTests()).containsExactly("AT1", "AT2");
        assertThat(result.getAutomationSkeleton()).contains("@Test");
        assertThat(result.getPlaywrightSkeleton()).contains("playwrightSkeleton".replace("playwrightSkeleton", "login"));
        assertThat(result.getRiskLevel()).isEqualTo("High");
    }

    @Test
    void parseResponse_handlesNewlineEscapesInSkeletons() {
        String raw = buildRawOpenAiResponse("""
                {
                  "summary": "Feature.",
                  "testCases": ["TC1", "TC2"],
                  "edgeCases": ["EC1"],
                  "negativeTests": ["NT1"],
                  "securityTests": ["ST1"],
                  "apiTests": ["AT1"],
                  "automationSkeleton": "@Test\\npublic void test() {\\n  // step\\n}",
                  "playwrightSkeleton": "test('x', async ({ page }) => {\\n  await page.goto('/');\\n});",
                  "riskLevel": "Medium"
                }
                """);

        TestCaseResponse result = openAiService.parseResponse(raw);

        assertThat(result.getAutomationSkeleton()).contains("\n");
        assertThat(result.getPlaywrightSkeleton()).contains("\n");
    }

    @Test
    void parseResponse_throwsOnMalformedResponse() {
        String malformed = """
                { "output": [{ "content": [{ "text": "not valid json{{{" }] }] }
                """;

        assertThatThrownBy(() -> openAiService.parseResponse(malformed))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to parse");
    }

    @Test
    void parseResponse_throwsOnEmptyOutput() {
        String emptyOutput = """
                { "output": [] }
                """;

        assertThatThrownBy(() -> openAiService.parseResponse(emptyOutput))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Wraps a JSON payload in the OpenAI Responses API envelope format
     * that parseResponse() expects to unwrap.
     */
    private String buildRawOpenAiResponse(String jsonPayload) {
        // Escape the inner JSON so it can be embedded as a string value
        String escaped = jsonPayload.trim()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return """
                {
                  "output": [{
                    "content": [{
                      "text": "%s"
                    }]
                  }]
                }
                """.formatted(escaped);
    }
}
