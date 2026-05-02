package com.example.aitestgenerator.model;

import java.util.List;

public class TestCaseResponse {

    private String summary;
    private List<String> testCases;
    private List<String> edgeCases;
    private List<String> negativeTests;
    private List<String> securityTests;
    private List<String> apiTests;
    private String automationSkeleton;
    private String playwrightSkeleton;
    private String riskLevel;
    private double confidenceScore;

    public TestCaseResponse() {}

    public TestCaseResponse(List<String> testCases, List<String> edgeCases,
                            List<String> negativeTests, List<String> securityTests) {
        this.testCases = testCases;
        this.edgeCases = edgeCases;
        this.negativeTests = negativeTests;
        this.securityTests = securityTests;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getTestCases() { return testCases; }
    public void setTestCases(List<String> testCases) { this.testCases = testCases; }

    public List<String> getEdgeCases() { return edgeCases; }
    public void setEdgeCases(List<String> edgeCases) { this.edgeCases = edgeCases; }

    public List<String> getNegativeTests() { return negativeTests; }
    public void setNegativeTests(List<String> negativeTests) { this.negativeTests = negativeTests; }

    public List<String> getSecurityTests() { return securityTests; }
    public void setSecurityTests(List<String> securityTests) { this.securityTests = securityTests; }

    public List<String> getApiTests() { return apiTests; }
    public void setApiTests(List<String> apiTests) { this.apiTests = apiTests; }

    public String getAutomationSkeleton() { return automationSkeleton; }
    public void setAutomationSkeleton(String automationSkeleton) { this.automationSkeleton = automationSkeleton; }

    public String getPlaywrightSkeleton() { return playwrightSkeleton; }
    public void setPlaywrightSkeleton(String playwrightSkeleton) { this.playwrightSkeleton = playwrightSkeleton; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isEmpty() {
        return (testCases == null || testCases.isEmpty())
                && (edgeCases == null || edgeCases.isEmpty())
                && (negativeTests == null || negativeTests.isEmpty())
                && (securityTests == null || securityTests.isEmpty());
    }

    @Override
    public String toString() {
        return "TestCaseResponse{" +
                "summary='" + summary + '\'' +
                ", testCases=" + testCases +
                ", edgeCases=" + edgeCases +
                ", negativeTests=" + negativeTests +
                ", securityTests=" + securityTests +
                ", apiTests=" + apiTests +
                ", riskLevel='" + riskLevel + '\'' +
                ", confidenceScore=" + confidenceScore +
                '}';
    }
}
