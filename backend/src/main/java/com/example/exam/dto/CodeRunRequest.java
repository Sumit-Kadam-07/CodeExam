package com.example.exam.dto;

import lombok.Data;

@Data
public class CodeRunRequest {
    private String language; // Java, C, C++, Python, JavaScript
    private String sourceCode;
    private String input;
}

