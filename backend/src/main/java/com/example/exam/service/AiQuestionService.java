package com.example.exam.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.exam.ai.AiClient;
import com.example.exam.ai.PromptBuilder;
import com.example.exam.dto.AiGenerateRequest;
import com.example.exam.dto.AiGenerateResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
public class AiQuestionService {

    private static final Logger logger = LoggerFactory.getLogger(AiQuestionService.class);
    private final AiClient aiClient;
    private final PromptBuilder promptBuilder;
    private final Gson gson = new Gson();

    public AiQuestionService(AiClient aiClient, PromptBuilder promptBuilder) {
        this.aiClient = aiClient;
        this.promptBuilder = promptBuilder;
    }

    public AiGenerateResponse generateQuestion(AiGenerateRequest request) {
        String prompt = promptBuilder.buildQuestionPrompt(request);
        String rawResponse = aiClient.callApi(prompt);
        return parseAiResponse(rawResponse);
    }

    /**
     * Generate multiple questions from extracted file content.
     * The AI is instructed to parse each question independently and return a JSON array.
     */
    public List<AiGenerateResponse> generateMultipleQuestions(String extractedText, String language, String difficulty, int marks) {
        String prompt = promptBuilder.buildMultiQuestionPrompt(extractedText, language, difficulty, marks);
        String rawResponse = aiClient.callApi(prompt);
        return parseMultiQuestionResponse(rawResponse);
    }

    private AiGenerateResponse parseAiResponse(String rawResponse) {
        try {
            String json = rawResponse.trim();
            if (json.startsWith("```")) {
                json = json.replaceFirst("```json\\s*", "");
                json = json.replaceFirst("```\\s*", "");
                json = json.trim();
            }
            return gson.fromJson(json, AiGenerateResponse.class);
        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", rawResponse, e);
            throw new RuntimeException("Failed to parse AI response. Please try again.");
        }
    }

    private List<AiGenerateResponse> parseMultiQuestionResponse(String rawResponse) {
        try {
            String json = rawResponse.trim();
            if (json.startsWith("```")) {
                json = json.replaceFirst("```json\\s*", "");
                json = json.replaceFirst("```\\s*", "");
                json = json.trim();
            }
            // Try parsing as array first
            if (json.startsWith("[")) {
                java.lang.reflect.Type listType = new TypeToken<List<AiGenerateResponse>>(){}.getType();
                List<AiGenerateResponse> questions = gson.fromJson(json, listType);
                if (questions == null || questions.isEmpty()) {
                    logger.warn("AI returned empty array for multi-question request");
                    return new ArrayList<>();
                }
                // Filter out invalid entries
                List<AiGenerateResponse> valid = new ArrayList<>();
                for (AiGenerateResponse q : questions) {
                    if (q != null && q.getTitle() != null && !q.getTitle().isBlank()
                            && q.getProblemStatement() != null && !q.getProblemStatement().isBlank()) {
                        valid.add(q);
                    } else {
                        logger.warn("Skipping invalid question entry from AI response");
                    }
                }
                return valid;
            } else {
                // Fallback: try parsing as single object (backward compatibility)
                logger.warn("AI response is not a JSON array, trying single object parse");
                AiGenerateResponse single = parseAiResponse(rawResponse);
                if (single != null && single.getTitle() != null && !single.getTitle().isBlank()) {
                    List<AiGenerateResponse> result = new ArrayList<>();
                    result.add(single);
                    return result;
                }
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Failed to parse multi-question AI response: {}", rawResponse, e);
            throw new RuntimeException("Failed to parse AI response. The AI may not have returned valid JSON. Please try again.");
        }
    }
}
