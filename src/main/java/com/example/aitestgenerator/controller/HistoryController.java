package com.example.aitestgenerator.controller;

import com.example.aitestgenerator.model.TestRun;
import com.example.aitestgenerator.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
public class HistoryController {

    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<TestRun>> getHistory() {
        log.info("GET /api/tests/history");
        return ResponseEntity.ok(historyService.findAll());
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> exportMarkdown(@PathVariable Long id) {
        log.info("GET /api/tests/export/{}", id);
        byte[] bytes = historyService.exportAsMarkdown(id).getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-plan-" + id + ".md\"")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(bytes.length)
                .body(bytes);
    }

    @GetMapping("/export/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        log.info("GET /api/tests/export/{}/pdf", id);
        byte[] bytes = historyService.exportAsPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-plan-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(bytes.length)
                .body(bytes);
    }
}
