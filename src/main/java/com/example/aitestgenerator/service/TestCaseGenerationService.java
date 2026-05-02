package com.example.aitestgenerator.service;

import com.example.aitestgenerator.model.TestCaseResponse;
import com.example.aitestgenerator.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestCaseGenerationService {

    private static final Logger log = LoggerFactory.getLogger(TestCaseGenerationService.class);
    private static final int MAX_ATTEMPTS = 3;

    private final OpenAiService openAiService;
    private final ValidationService validationService;
    private final HistoryService historyService;

    public TestCaseGenerationService(OpenAiService openAiService,
                                     ValidationService validationService,
                                     HistoryService historyService) {
        this.openAiService = openAiService;
        this.validationService = validationService;
        this.historyService = historyService;
    }

    public TestCaseResponse generate(String feature) {
        log.info("Starting test case generation for feature: {}", feature);

        String lastRawResponse = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            log.info("Attempt {}/{}", attempt, MAX_ATTEMPTS);
            try {
                if (attempt == 1) {
                    lastRawResponse = openAiService.callOpenAiRaw(
                            "Generate test cases as JSON for this feature: " + feature);
                } else {
                    lastRawResponse = openAiService.callOpenAiRaw(
                            "The previous JSON response was invalid or incomplete. " +
                            "Fix it and return only valid JSON with all nine required keys " +
                            "(summary, testCases, edgeCases, negativeTests, securityTests, apiTests, automationSkeleton, playwrightSkeleton, riskLevel). " +
                            "Feature: " + feature + ". Previous bad response: " + lastRawResponse);
                }

                TestCaseResponse lastResponse = openAiService.parseResponse(lastRawResponse);
                ValidationResult validation = validationService.validate(lastResponse);

                log.info("Attempt {} validation: {}", attempt, validation);

                if (validation.isValid()) {
                    double score = calculateConfidenceScore(lastResponse);
                    lastResponse.setConfidenceScore(score);
                    log.info("Generation succeeded on attempt {}. Confidence score: {}", attempt, score);
                    historyService.save(feature, lastResponse);
                    return lastResponse;
                } else {
                    log.warn("Attempt {} failed validation: {}", attempt, validation.getErrors());
                }

            } catch (Exception e) {
                log.error("Attempt {} threw an exception: {}", attempt, e.getMessage());
            }
        }

        log.warn("All {} attempts failed. Returning fallback response for feature: {}", MAX_ATTEMPTS, feature);
        return fallbackResponse();
    }

    private double calculateConfidenceScore(TestCaseResponse response) {
        double score = 0.0;

        score += 0.25; // valid JSON parsed

        boolean allPresent = isNonEmpty(response.getTestCases())
                && isNonEmpty(response.getEdgeCases())
                && isNonEmpty(response.getNegativeTests())
                && isNonEmpty(response.getSecurityTests());
        if (allPresent) score += 0.25;

        boolean allHaveEnough = hasMinEntries(response.getTestCases(), 3)
                && hasMinEntries(response.getEdgeCases(), 3)
                && hasMinEntries(response.getNegativeTests(), 3)
                && hasMinEntries(response.getSecurityTests(), 3);
        if (allHaveEnough) score += 0.25;

        if (isNonEmpty(response.getSecurityTests())) score += 0.15;

        if (!isNonEmpty(response.getTestCases())) score -= 0.10;
        if (!isNonEmpty(response.getEdgeCases())) score -= 0.10;
        if (!isNonEmpty(response.getNegativeTests())) score -= 0.10;
        if (!isNonEmpty(response.getSecurityTests())) score -= 0.10;

        return Math.max(0.0, Math.min(1.0, Math.round(score * 100.0) / 100.0));
    }

    private boolean isNonEmpty(List<String> list) {
        return list != null && !list.isEmpty();
    }

    private boolean hasMinEntries(List<String> list, int min) {
        return list != null && list.size() >= min;
    }

    private TestCaseResponse fallbackResponse() {
        TestCaseResponse fallback = new TestCaseResponse(
                List.of("Unable to generate reliable test cases. Please refine the feature description."),
                List.of(),
                List.of(),
                List.of()
        );
        fallback.setConfidenceScore(0.0);
        return fallback;
    }
}
