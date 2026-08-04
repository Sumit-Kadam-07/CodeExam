package com.example.exam.dto;

import lombok.Data;

@Data
public class UploadQuestionResponse {
    private String originalFilename;
    private String fileType;
    private String extractedText;
    private String processingStatus;
    private AiGenerateResponse aiResult;
}
