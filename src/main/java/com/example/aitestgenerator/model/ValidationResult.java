package com.example.aitestgenerator.model;

import java.util.List;

public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    public ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult fail(List<String> errors) {
        return new ValidationResult(false, errors);
    }

    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }

    @Override
    public String toString() {
        return valid ? "VALID" : "INVALID: " + errors;
    }
}
