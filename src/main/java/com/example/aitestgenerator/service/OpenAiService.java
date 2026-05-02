package com.example.aitestgenerator.service;

import com.example.aitestgenerator.model.TestCaseResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    private static final String SYSTEM_PROMPT = """
            You are a senior QA engineer. Generate a comprehensive test plan for the given feature.
            Return ONLY valid JSON with exactly these nine keys:
            - summary: one concise sentence describing what the feature does
            - testCases: array of functional test case descriptions
            - edgeCases: array of edge case descriptions
            - negativeTests: array of negative test descriptions
            - securityTests: array of security test descriptions
            - apiTests: array of API-level test scenarios covering status codes, payloads, auth, and boundary values
            - automationSkeleton: a Java TestNG @Test method stub as a single string (use \\n for newlines)
            - playwrightSkeleton: a Playwright (TypeScript) test stub as a single string (use \\n for newlines)
            - riskLevel: exactly one of "Low", "Medium", or "High"
            Each array value must be a non-empty array of strings.
            Do not include any explanation or markdown — only the raw JSON object.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAiService(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${openai.model:gpt-4o}") String model,
            ObjectMapper objectMapper) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public TestCaseResponse generateTestCases(String feature) {
        log.info("Sending request to OpenAI for feature: {}", feature);
        return callOpenAi("Generate test cases as JSON for this feature: " + feature);
    }

    public TestCaseResponse generateWithFixPrompt(String feature, String badJson) {
        log.info("Sending fix-prompt to OpenAI for feature: {}", feature);
        String input = "The previous JSON response was invalid or incomplete. " +
                "Fix it and return only valid JSON with all four required keys " +
                "(testCases, edgeCases, negativeTests, securityTests), each as a non-empty array of strings. " +
                "Feature: " + feature + ". Previous bad response: " + badJson;
        return callOpenAi(input);
    }

    private TestCaseResponse callOpenAi(String input) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "instructions", SYSTEM_PROMPT,
                "input", input,
                "text", Map.of("format", Map.of("type", "json_object"))
        );

        String rawResponse = restClient.post()
                .uri("/v1/responses")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        log.debug("Raw OpenAI response: {}", rawResponse);
        return parseResponse(rawResponse);
    }

    public String callOpenAiRaw(String input) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "instructions", SYSTEM_PROMPT,
                "input", input,
                "text", Map.of("format", Map.of("type", "json_object"))
        );

        return restClient.post()
                .uri("/v1/responses")
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }

    public TestCaseResponse parseResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String jsonText = root
                    .path("output").get(0)
                    .path("content").get(0)
                    .path("text").asText();

            return objectMapper.readValue(jsonText, TestCaseResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", e.getMessage());
            throw new IllegalStateException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }
}
