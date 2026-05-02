package com.example.aitestgenerator.controller;

import com.example.aitestgenerator.model.TestCaseRequest;
import com.example.aitestgenerator.model.TestCaseResponse;
import com.example.aitestgenerator.service.TestCaseGenerationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tests")
public class TestCaseController {

    private static final Logger log = LoggerFactory.getLogger(TestCaseController.class);

    private final TestCaseGenerationService generationService;

    public TestCaseController(TestCaseGenerationService generationService) {
        this.generationService = generationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TestCaseResponse> generate(@Valid @RequestBody TestCaseRequest request) {
        log.info("POST /api/tests/generate — feature: {}", request.feature());
        TestCaseResponse response = generationService.generate(request.feature());
        log.info("Returning response with confidence score: {}", response.getConfidenceScore());
        return ResponseEntity.ok(response);
    }
}
