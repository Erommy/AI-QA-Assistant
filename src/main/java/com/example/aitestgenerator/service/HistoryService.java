package com.example.aitestgenerator.service;

import com.example.aitestgenerator.model.TestCaseResponse;
import com.example.aitestgenerator.model.TestRun;
import com.example.aitestgenerator.repository.TestRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class HistoryService {

    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);

    private static final Font FONT_TITLE    = new Font(Font.HELVETICA, 18, Font.BOLD,   new Color(45, 55, 72));
    private static final Font FONT_HEADING  = new Font(Font.HELVETICA, 13, Font.BOLD,   new Color(66, 153, 225));
    private static final Font FONT_LABEL    = new Font(Font.HELVETICA, 10, Font.BOLD,   new Color(74, 85, 104));
    private static final Font FONT_BODY     = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(26, 32, 44));
    private static final Font FONT_CODE     = new Font(Font.COURIER,    9, Font.NORMAL, new Color(45, 55, 72));
    private static final Font FONT_BULLET   = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(26, 32, 44));

    private final TestRunRepository repository;
    private final ObjectMapper objectMapper;

    public HistoryService(TestRunRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public TestRun save(String feature, TestCaseResponse response) {
        try {
            TestRun run = new TestRun();
            run.setFeature(feature);
            run.setGeneratedJson(objectMapper.writeValueAsString(response));
            run.setConfidenceScore(response.getConfidenceScore());
            run.setRiskLevel(response.getRiskLevel());
            TestRun saved = repository.save(run);
            log.info("Saved test run id={} for feature: {}", saved.getId(), feature);
            return saved;
        } catch (Exception e) {
            log.error("Failed to save test run: {}", e.getMessage());
            throw new IllegalStateException("Failed to save test run", e);
        }
    }

    public List<TestRun> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public String exportAsMarkdown(Long id) {
        TestRun run = findRunById(id);
        try {
            TestCaseResponse response = objectMapper.readValue(run.getGeneratedJson(), TestCaseResponse.class);
            return buildMarkdown(run, response);
        } catch (Exception e) {
            log.error("Failed to export markdown for run {}: {}", id, e.getMessage());
            throw new IllegalStateException("Failed to export test run", e);
        }
    }

    public byte[] exportAsPdf(Long id) {
        TestRun run = findRunById(id);
        try {
            TestCaseResponse response = objectMapper.readValue(run.getGeneratedJson(), TestCaseResponse.class);
            return buildPdf(run, response);
        } catch (Exception e) {
            log.error("Failed to export PDF for run {}: {}", id, e.getMessage());
            throw new IllegalStateException("Failed to export PDF", e);
        }
    }

    private TestRun findRunById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test run not found: " + id));
    }

    // ── PDF builder ──────────────────────────────────────────────────────────

    private byte[] buildPdf(TestRun run, TestCaseResponse r) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter.getInstance(doc, out);
        doc.open();

        doc.add(new Paragraph("Test Plan", FONT_TITLE));
        doc.add(Chunk.NEWLINE);

        addMeta(doc, "Feature",          run.getFeature());
        if (r.getSummary() != null) addMeta(doc, "Summary", r.getSummary());
        addMeta(doc, "Risk Level",       orNa(r.getRiskLevel()));
        addMeta(doc, "Confidence Score", String.format("%.0f%%", r.getConfidenceScore() * 100));
        addMeta(doc, "Generated",        run.getCreatedAt().toString());

        doc.add(new Paragraph(" "));

        addSection(doc, "Test Cases",     r.getTestCases());
        addSection(doc, "Edge Cases",     r.getEdgeCases());
        addSection(doc, "Negative Tests", r.getNegativeTests());
        addSection(doc, "Security Tests", r.getSecurityTests());
        addSection(doc, "API Tests",      r.getApiTests());
        addCodeSection(doc, "Automation Skeleton — TestNG (Java)",       r.getAutomationSkeleton());
        addCodeSection(doc, "Automation Skeleton — Playwright (TypeScript)", r.getPlaywrightSkeleton());

        doc.close();
        return out.toByteArray();
    }

    private void addMeta(Document doc, String label, String value) throws Exception {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", FONT_LABEL));
        p.add(new Chunk(value, FONT_BODY));
        p.setSpacingAfter(2);
        doc.add(p);
    }

    private void addSection(Document doc, String title, List<String> items) throws Exception {
        doc.add(new Paragraph(title, FONT_HEADING));
        if (items == null || items.isEmpty()) {
            doc.add(new Paragraph("None generated.", FONT_BODY));
        } else {
            for (String item : items) {
                Paragraph p = new Paragraph("• " + item, FONT_BULLET);
                p.setIndentationLeft(12);
                p.setSpacingAfter(2);
                doc.add(p);
            }
        }
        doc.add(new Paragraph(" "));
    }

    private void addCodeSection(Document doc, String title, String code) throws Exception {
        if (code == null || code.isBlank()) return;
        doc.add(new Paragraph(title, FONT_HEADING));
        Paragraph p = new Paragraph(code, FONT_CODE);
        p.setIndentationLeft(12);
        doc.add(p);
        doc.add(new Paragraph(" "));
    }

    // ── Markdown builder ─────────────────────────────────────────────────────

    private String buildMarkdown(TestRun run, TestCaseResponse r) {
        StringBuilder md = new StringBuilder();
        md.append("# Test Plan\n\n");
        md.append("**Feature:** ").append(run.getFeature()).append("\n\n");
        if (r.getSummary() != null) {
            md.append("**Summary:** ").append(r.getSummary()).append("\n\n");
        }
        md.append("**Risk Level:** ").append(orNa(r.getRiskLevel())).append("  \n");
        md.append("**Confidence Score:** ").append(r.getConfidenceScore()).append("  \n");
        md.append("**Generated:** ").append(run.getCreatedAt()).append("\n\n---\n\n");

        appendMdSection(md, "Test Cases",     r.getTestCases());
        appendMdSection(md, "Edge Cases",     r.getEdgeCases());
        appendMdSection(md, "Negative Tests", r.getNegativeTests());
        appendMdSection(md, "Security Tests", r.getSecurityTests());
        appendMdSection(md, "API Tests",      r.getApiTests());

        if (r.getAutomationSkeleton() != null && !r.getAutomationSkeleton().isBlank()) {
            md.append("## Automation Skeleton — TestNG (Java)\n\n```java\n")
              .append(r.getAutomationSkeleton()).append("\n```\n\n");
        }
        if (r.getPlaywrightSkeleton() != null && !r.getPlaywrightSkeleton().isBlank()) {
            md.append("## Automation Skeleton — Playwright (TypeScript)\n\n```typescript\n")
              .append(r.getPlaywrightSkeleton()).append("\n```\n");
        }

        return md.toString();
    }

    private void appendMdSection(StringBuilder md, String title, List<String> items) {
        md.append("## ").append(title).append("\n\n");
        if (items == null || items.isEmpty()) {
            md.append("_None generated._\n\n");
        } else {
            items.forEach(item -> md.append("- ").append(item).append("\n"));
            md.append("\n");
        }
    }

    private String orNa(String value) {
        return value != null ? value : "N/A";
    }
}
