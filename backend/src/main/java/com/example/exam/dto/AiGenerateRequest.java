package com.example.exam.dto;

import lombok.Data;

@Data
public class AiGenerateRequest {
    private String topic;
    private String language;
    private String difficulty;
    private int marks;
}
