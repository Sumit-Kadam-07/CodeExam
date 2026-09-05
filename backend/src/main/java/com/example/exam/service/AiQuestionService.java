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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

@Service
public class AiQuestionService {

    private static final Logger logger =
            LoggerFactory.getLogger(AiQuestionService.class);

    private final AiClient aiClient;
    private final PromptBuilder promptBuilder;
    private final Gson gson = new Gson();

    public AiQuestionService(
            AiClient aiClient,
            PromptBuilder promptBuilder) {

        this.aiClient = aiClient;
        this.promptBuilder = promptBuilder;
    }

    public AiGenerateResponse generateQuestion(
            AiGenerateRequest request) {

        String prompt =
                promptBuilder.buildQuestionPrompt(request);

        String rawResponse =
                aiClient.callApi(prompt);

        return parseAiResponse(rawResponse);
    }

    /**
     * Generate multiple questions from uploaded file content.
     */
    public List<AiGenerateResponse> generateMultipleQuestions(
            String extractedText,
            String language,
            String difficulty,
            int marks) {

        String prompt =
                promptBuilder.buildMultiQuestionPrompt(
                        extractedText,
                        language,
                        difficulty,
                        marks);

        String rawResponse =
                aiClient.callApi(prompt);

        return parseMultiQuestionResponse(rawResponse);
    }

    /**
     * Parse a single AI-generated question.
     */
    private AiGenerateResponse parseAiResponse(
            String rawResponse) {

        try {

            String json = rawResponse.trim();

            // Remove markdown code fences if returned by AI
            if (json.startsWith("```")) {

                json = json.replaceFirst(
                        "```json\\s*",
                        "");

                json = json.replaceFirst(
                        "```\\s*",
                        "");

                json = json.trim();
            }

            return gson.fromJson(
                    json,
                    AiGenerateResponse.class);

        } catch (Exception e) {

            logger.error(
                    "Failed to parse AI response: {}",
                    rawResponse,
                    e);

            throw new RuntimeException(
                    "Failed to parse AI response. Please try again.");
        }
    }

    /**
     * Parse multiple questions returned as:
     *
     * {
     *   "questions": [
     *      {...},
     *      {...}
     *   ]
     * }
     */
    private List<AiGenerateResponse> parseMultiQuestionResponse(
            String rawResponse) {

        try {

            String json = rawResponse.trim();

            // Remove markdown code fences if returned by AI
            if (json.startsWith("```")) {

                json = json.replaceFirst(
                        "```json\\s*",
                        "");

                json = json.replaceFirst(
                        "```\\s*",
                        "");

                json = json.trim();
            }

            // Parse root JSON object
            JsonObject root =
                    JsonParser.parseString(json)
                            .getAsJsonObject();

            // Get questions array
            JsonArray questionsArray =
                    root.getAsJsonArray("questions");

            if (questionsArray == null) {

                logger.warn(
                        "AI response does not contain 'questions' array");

                return new ArrayList<>();
            }

            java.lang.reflect.Type listType =
                    new TypeToken<List<AiGenerateResponse>>() {
                    }.getType();

            List<AiGenerateResponse> questions =
                    gson.fromJson(
                            questionsArray,
                            listType);

            if (questions == null || questions.isEmpty()) {

                logger.warn(
                        "AI returned empty questions array");

                return new ArrayList<>();
            }

            // Filter invalid questions
            List<AiGenerateResponse> valid =
                    new ArrayList<>();

            for (AiGenerateResponse q : questions) {

                if (q != null
                        && q.getTitle() != null
                        && !q.getTitle().isBlank()
                        && q.getProblemStatement() != null
                        && !q.getProblemStatement().isBlank()) {

                    valid.add(q);

                } else {

                    logger.warn(
                            "Skipping invalid question entry from AI response");
                }
            }

            logger.info(
                    "Successfully parsed {} AI-generated questions",
                    valid.size());

            return valid;

        } catch (Exception e) {

            logger.error(
                    "Failed to parse multi-question AI response: {}",
                    rawResponse,
                    e);

            throw new RuntimeException(
                    "Failed to parse AI response. "
                    + "The AI returned invalid structured data.");
        }
    }
}
