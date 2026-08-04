package com.example.exam.dto;

import lombok.Data;

@Data
public class CodeCompileRequest {
    private String language; // Java, C, C++, Python, JavaScript
    private String sourceCode;
}

