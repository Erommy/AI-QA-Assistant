package com.example.aitestgenerator.service;

import com.example.aitestgenerator.model.TestCaseResponse;
import com.example.aitestgenerator.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestCaseGenerationServiceTest {

    @Mock
    private OpenAiService openAiService;

    @Mock
    private ValidationService validationService;

    @Mock
    private HistoryService historyService;

    private TestCaseGenerationService generationService;

    @BeforeEach
    void setUp() {
        generationService = new TestCaseGenerationService(openAiService, validationService, historyService);
    }

    @Test
    void firstAttemptValid_returnsImmediatelyWithScore() {
        TestCaseResponse validResponse = buildValidResponse();
        when(openAiService.callOpenAiRaw(anyString())).thenReturn("raw-json");
        when(openAiService.parseResponse("raw-json")).thenReturn(validResponse);
        when(validationService.validate(validResponse)).thenReturn(ValidationResult.ok());

        TestCaseResponse result = generationService.generate("reset password feature");

        assertThat(result).isNotNull();
        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
        verify(openAiService, times(1)).callOpenAiRaw(anyString());
    }

    @Test
    void firstAttemptInvalid_secondAttemptValid_retriesOnce() {
        TestCaseResponse invalidResponse = buildInvalidResponse();
        TestCaseResponse validResponse = buildValidResponse();

        when(openAiService.callOpenAiRaw(anyString())).thenReturn("raw-json");
        when(openAiService.parseResponse("raw-json"))
                .thenReturn(invalidResponse)
                .thenReturn(validResponse);
        when(validationService.validate(invalidResponse))
                .thenReturn(ValidationResult.fail(List.of("testCases is missing or empty")));
        when(validationService.validate(validResponse))
                .thenReturn(ValidationResult.ok());

        TestCaseResponse result = generationService.generate("reset password feature");

        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
        verify(openAiService, times(2)).callOpenAiRaw(anyString());
    }

    @Test
    void allAttemptsInvalid_returnsFallbackResponse() {
        TestCaseResponse invalidResponse = buildInvalidResponse();

        when(openAiService.callOpenAiRaw(anyString())).thenReturn("raw-json");
        when(openAiService.parseResponse("raw-json")).thenReturn(invalidResponse);
        when(validationService.validate(any())).thenReturn(
                ValidationResult.fail(List.of("testCases is missing or empty")));

        TestCaseResponse result = generationService.generate("reset password feature");

        assertThat(result.getConfidenceScore()).isEqualTo(0.0);
        assertThat(result.getTestCases()).hasSize(1);
        assertThat(result.getTestCases().get(0)).contains("Unable to generate");
        verify(openAiService, times(3)).callOpenAiRaw(anyString());
    }

    @Test
    void openAiThrowsException_continuesToNextAttempt() {
        TestCaseResponse validResponse = buildValidResponse();

        when(openAiService.callOpenAiRaw(anyString()))
                .thenThrow(new RuntimeException("OpenAI timeout"))
                .thenReturn("raw-json");
        when(openAiService.parseResponse("raw-json")).thenReturn(validResponse);
        when(validationService.validate(validResponse)).thenReturn(ValidationResult.ok());

        TestCaseResponse result = generationService.generate("reset password feature");

        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
        verify(openAiService, times(2)).callOpenAiRaw(anyString());
    }

    @Test
    void confidenceScore_allSectionsFullyPopulated_isHigh() {
        TestCaseResponse response = new TestCaseResponse(
                List.of("T1", "T2", "T3", "T4"),
                List.of("E1", "E2", "E3"),
                List.of("N1", "N2", "N3"),
                List.of("S1", "S2", "S3")
        );

        when(openAiService.callOpenAiRaw(anyString())).thenReturn("raw");
        when(openAiService.parseResponse("raw")).thenReturn(response);
        when(validationService.validate(response)).thenReturn(ValidationResult.ok());

        TestCaseResponse result = generationService.generate("feature");

        assertThat(result.getConfidenceScore()).isEqualTo(0.9);
    }

    @Test
    void confidenceScore_someEmptySections_isLower() {
        TestCaseResponse response = new TestCaseResponse(
                List.of("T1", "T2", "T3"),
                List.of("E1", "E2"),
                List.of(),
                List.of()
        );
        response.setSecurityTests(null);
        response.setNegativeTests(null);

        when(openAiService.callOpenAiRaw(anyString())).thenReturn("raw");
        when(openAiService.parseResponse("raw")).thenReturn(response);
        when(validationService.validate(response)).thenReturn(ValidationResult.ok());

        TestCaseResponse result = generationService.generate("feature");

        assertThat(result.getConfidenceScore()).isLessThan(0.9);
    }

    @Test
    void successfulGeneration_savesHistoryOnce() {
        TestCaseResponse validResponse = buildValidResponse();
        when(openAiService.callOpenAiRaw(anyString())).thenReturn("raw-json");
        when(openAiService.parseResponse("raw-json")).thenReturn(validResponse);
        when(validationService.validate(validResponse)).thenReturn(ValidationResult.ok());

        generationService.generate("login feature");

        verify(historyService, times(1)).save(eq("login feature"), eq(validResponse));
    }

    @Test
    void fallbackResponse_doesNotSaveHistory() {
        TestCaseResponse invalidResponse = buildInvalidResponse();
        when(openAiService.callOpenAiRaw(anyString())).thenReturn("raw-json");
        when(openAiService.parseResponse("raw-json")).thenReturn(invalidResponse);
        when(validationService.validate(any())).thenReturn(
                ValidationResult.fail(List.of("testCases is missing or empty")));

        generationService.generate("login feature");

        verify(historyService, never()).save(anyString(), any());
    }

    private TestCaseResponse buildValidResponse() {
        TestCaseResponse r = new TestCaseResponse(
                List.of("Test 1", "Test 2", "Test 3"),
                List.of("Edge 1", "Edge 2", "Edge 3"),
                List.of("Neg 1", "Neg 2", "Neg 3"),
                List.of("Sec 1", "Sec 2", "Sec 3")
        );
        r.setSummary("A test feature.");
        r.setApiTests(List.of("API 1", "API 2"));
        r.setAutomationSkeleton("@Test\npublic void test() {}");
        r.setPlaywrightSkeleton("test('feature', async ({ page }) => {});");
        r.setRiskLevel("Medium");
        return r;
    }

    private TestCaseResponse buildInvalidResponse() {
        TestCaseResponse r = new TestCaseResponse();
        r.setTestCases(null);
        return r;
    }
}
