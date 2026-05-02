package com.example.aitestgenerator.model;

import jakarta.validation.constraints.NotBlank;

public record TestCaseRequest(
        @NotBlank(message = "Feature description must not be blank")
        String feature
) {}
