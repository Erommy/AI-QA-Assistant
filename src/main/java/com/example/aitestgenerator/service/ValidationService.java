package com.example.aitestgenerator.service;

import com.example.aitestgenerator.model.TestCaseResponse;
import com.example.aitestgenerator.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationService {

    private static final int MIN_ENTRIES = 2;
    private static final int MAX_ENTRY_LENGTH = 300;
    private static final List<String> VALID_RISK_LEVELS = List.of("Low", "Medium", "High");

    public ValidationResult validate(TestCaseResponse response) {
        List<String> errors = new ArrayList<>();

        checkSection(response.getTestCases(), "testCases", errors);
        checkSection(response.getEdgeCases(), "edgeCases", errors);
        checkSection(response.getNegativeTests(), "negativeTests", errors);
        checkSection(response.getSecurityTests(), "securityTests", errors);
        checkSection(response.getApiTests(), "apiTests", errors);

        if (response.getSummary() == null || response.getSummary().isBlank()) {
            errors.add("summary is missing or blank");
        }

        if (response.getAutomationSkeleton() == null || response.getAutomationSkeleton().isBlank()) {
            errors.add("automationSkeleton is missing or blank");
        }

        if (response.getPlaywrightSkeleton() == null || response.getPlaywrightSkeleton().isBlank()) {
            errors.add("playwrightSkeleton is missing or blank");
        }

        if (response.getRiskLevel() == null || !VALID_RISK_LEVELS.contains(response.getRiskLevel())) {
            errors.add("riskLevel must be one of: Low, Medium, High");
        }

        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    private void checkSection(List<String> entries, String name, List<String> errors) {
        if (entries == null || entries.isEmpty()) {
            errors.add(name + " is missing or empty");
            return;
        }
        if (entries.size() < MIN_ENTRIES) {
            errors.add(name + " has fewer than " + MIN_ENTRIES + " entries");
        }
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                errors.add(name + " contains a blank entry");
                break;
            }
            if (entry.length() > MAX_ENTRY_LENGTH) {
                errors.add(name + " contains an entry that looks like explanation text (> " + MAX_ENTRY_LENGTH + " chars)");
                break;
            }
        }
    }
}
