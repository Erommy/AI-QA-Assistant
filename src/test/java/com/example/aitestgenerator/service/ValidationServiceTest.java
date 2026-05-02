package com.example.aitestgenerator.service;

import com.example.aitestgenerator.model.TestCaseResponse;
import com.example.aitestgenerator.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    void validResponse_allFieldsPresent_passesValidation() {
        TestCaseResponse response = buildFullyValidResponse();

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void missingTestCases_failsValidation() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setTestCases(null);

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("testCases"));
    }

    @Test
    void emptyEdgeCases_failsValidation() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setEdgeCases(List.of());

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("edgeCases"));
    }

    @Test
    void sectionBelowMinimumEntries_failsValidation() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setTestCases(List.of("Only one test"));

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("testCases") && e.contains("fewer than"));
    }

    @Test
    void entryExceedingMaxLength_failsValidation() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setTestCases(List.of("x".repeat(301), "Test 2"));

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("explanation text"));
    }

    @Test
    void missingSummary_failsValidation() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setSummary(null);

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("summary"));
    }

    @Test
    void missingApiTests_failsValidation() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setApiTests(null);

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("apiTests"));
    }

    @Test
    void missingAutomationSkeleton_failsValidation() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setAutomationSkeleton(null);

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("automationSkeleton"));
    }

    @Test
    void missingPlaywrightSkeleton_failsValidation() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setPlaywrightSkeleton(null);

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("playwrightSkeleton"));
    }

    @Test
    void invalidRiskLevel_failsValidation() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setRiskLevel("Critical");

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("riskLevel"));
    }

    @Test
    void validRiskLevels_allAccepted() {
        for (String level : List.of("Low", "Medium", "High")) {
            TestCaseResponse response = buildFullyValidResponse();
            response.setRiskLevel(level);
            assertThat(validationService.validate(response).isValid())
                    .as("Expected riskLevel '%s' to be valid", level)
                    .isTrue();
        }
    }

    @Test
    void multipleFailingSections_reportsAllErrors() {
        TestCaseResponse response = buildFullyValidResponse();
        response.setTestCases(null);
        response.setEdgeCases(null);
        response.setSummary(null);

        ValidationResult result = validationService.validate(response);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(result.getErrors()).anyMatch(e -> e.contains("testCases"));
        assertThat(result.getErrors()).anyMatch(e -> e.contains("edgeCases"));
        assertThat(result.getErrors()).anyMatch(e -> e.contains("summary"));
    }

    private TestCaseResponse buildFullyValidResponse() {
        TestCaseResponse r = new TestCaseResponse();
        r.setSummary("A feature that allows users to reset their password.");
        r.setTestCases(List.of("Test 1", "Test 2", "Test 3"));
        r.setEdgeCases(List.of("Edge 1", "Edge 2"));
        r.setNegativeTests(List.of("Neg 1", "Neg 2"));
        r.setSecurityTests(List.of("Sec 1", "Sec 2"));
        r.setApiTests(List.of("API 1", "API 2"));
        r.setAutomationSkeleton("@Test\npublic void test() {}");
        r.setPlaywrightSkeleton("test('feature', async ({ page }) => {});");
        r.setRiskLevel("Medium");
        return r;
    }
}
