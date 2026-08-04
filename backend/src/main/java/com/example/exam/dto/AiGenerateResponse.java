package com.example.exam.dto;

import java.util.List;

import lombok.Data;

@Data
public class AiGenerateResponse {
    private String title;
    private String problemStatement;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private String sampleInput;
    private String sampleOutput;
    private String explanation;
    private String starterCode;
    private String expectedSolution;
    private String difficulty;
    private int marks;
    private List<TestCaseData> testCases;

    @Data
    public static class TestCaseData {
        private String input;
        private String expectedOutput;
        private int weight;
        private boolean sample;
    }
}
