package com.example.aitestgenerator.service;

import com.example.aitestgenerator.model.TestCaseResponse;
import com.example.aitestgenerator.model.TestRun;
import com.example.aitestgenerator.repository.TestRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private TestRunRepository repository;

    private HistoryService historyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        historyService = new HistoryService(repository, objectMapper);
    }

    // ── save() ───────────────────────────────────────────────────────────────

    @Test
    void save_persistsCorrectFields() {
        TestCaseResponse response = buildFullResponse();
        TestRun persisted = stubSave();

        historyService.save("login feature", response);

        ArgumentCaptor<TestRun> captor = ArgumentCaptor.forClass(TestRun.class);
        verify(repository).save(captor.capture());

        TestRun saved = captor.getValue();
        assertThat(saved.getFeature()).isEqualTo("login feature");
        assertThat(saved.getConfidenceScore()).isEqualTo(0.85);
        assertThat(saved.getRiskLevel()).isEqualTo("High");
        assertThat(saved.getGeneratedJson()).contains("testCases");
    }

    @Test
    void save_returnsPersistedEntity() {
        TestCaseResponse response = buildFullResponse();
        TestRun persisted = stubSave();

        TestRun result = historyService.save("feature", response);

        assertThat(result).isEqualTo(persisted);
    }

    // ── findAll() ────────────────────────────────────────────────────────────

    @Test
    void findAll_delegatesToRepository() throws Exception {
        TestRun run = buildTestRun(buildFullResponse());
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(run));

        List<TestRun> results = historyService.findAll();

        assertThat(results).hasSize(1);
        verify(repository).findAllByOrderByCreatedAtDesc();
    }

    // ── exportAsMarkdown() ───────────────────────────────────────────────────

    @Test
    void exportAsMarkdown_containsAllSectionHeadings() throws Exception {
        TestRun run = buildTestRun(buildFullResponse());
        when(repository.findById(1L)).thenReturn(Optional.of(run));

        String md = historyService.exportAsMarkdown(1L);

        assertThat(md).contains("# Test Plan");
        assertThat(md).contains("**Feature:**");
        assertThat(md).contains("**Summary:**");
        assertThat(md).contains("**Risk Level:** High");
        assertThat(md).contains("## Test Cases");
        assertThat(md).contains("## Edge Cases");
        assertThat(md).contains("## Negative Tests");
        assertThat(md).contains("## Security Tests");
        assertThat(md).contains("## API Tests");
        assertThat(md).contains("TestNG");
        assertThat(md).contains("Playwright");
    }

    @Test
    void exportAsMarkdown_containsActualItems() throws Exception {
        TestRun run = buildTestRun(buildFullResponse());
        when(repository.findById(1L)).thenReturn(Optional.of(run));

        String md = historyService.exportAsMarkdown(1L);

        assertThat(md).contains("Test case 1");
        assertThat(md).contains("Edge case 1");
        assertThat(md).contains("testMethod()");
        assertThat(md).contains("playwrightTest()");
    }

    @Test
    void exportAsMarkdown_throwsWhenIdNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.exportAsMarkdown(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // ── exportAsPdf() ────────────────────────────────────────────────────────

    @Test
    void exportAsPdf_returnsNonEmptyByteArray() throws Exception {
        TestRun run = buildTestRun(buildFullResponse());
        when(repository.findById(1L)).thenReturn(Optional.of(run));

        byte[] pdf = historyService.exportAsPdf(1L);

        assertThat(pdf).isNotEmpty();
    }

    @Test
    void exportAsPdf_startsWithPdfHeader() throws Exception {
        TestRun run = buildTestRun(buildFullResponse());
        when(repository.findById(1L)).thenReturn(Optional.of(run));

        byte[] pdf = historyService.exportAsPdf(1L);

        String header = new String(pdf, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    void exportAsPdf_throwsWhenIdNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.exportAsPdf(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private TestRun stubSave() {
        TestRun persisted = new TestRun();
        when(repository.save(any(TestRun.class))).thenReturn(persisted);
        return persisted;
    }

    private TestRun buildTestRun(TestCaseResponse response) throws Exception {
        TestRun run = new TestRun();
        run.setFeature("login feature");
        run.setGeneratedJson(objectMapper.writeValueAsString(response));
        run.setConfidenceScore(response.getConfidenceScore());
        run.setRiskLevel(response.getRiskLevel());

        // Simulate @PrePersist
        var field = TestRun.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(run, LocalDateTime.now());

        return run;
    }

    private TestCaseResponse buildFullResponse() {
        TestCaseResponse r = new TestCaseResponse();
        r.setSummary("A feature that allows users to log in.");
        r.setTestCases(List.of("Test case 1", "Test case 2"));
        r.setEdgeCases(List.of("Edge case 1", "Edge case 2"));
        r.setNegativeTests(List.of("Neg test 1", "Neg test 2"));
        r.setSecurityTests(List.of("Sec test 1", "Sec test 2"));
        r.setApiTests(List.of("API test 1", "API test 2"));
        r.setAutomationSkeleton("@Test\npublic void testMethod() {}");
        r.setPlaywrightSkeleton("test('feature', async () => { playwrightTest(); });");
        r.setRiskLevel("High");
        r.setConfidenceScore(0.85);
        return r;
    }
}
