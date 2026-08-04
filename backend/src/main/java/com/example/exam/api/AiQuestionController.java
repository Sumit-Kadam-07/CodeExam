package com.example.exam.api;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exam.dto.AiGenerateRequest;
import com.example.exam.dto.AiGenerateResponse;
import com.example.exam.service.AiQuestionService;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasRole('ADMIN')")
public class AiQuestionController {

    private static final Logger logger = LoggerFactory.getLogger(AiQuestionController.class);
    private final AiQuestionService aiQuestionService;

    public AiQuestionController(AiQuestionService aiQuestionService) {
        this.aiQuestionService = aiQuestionService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateQuestion(@RequestBody AiGenerateRequest request) {
        logger.info("API: AI generate question - topic='{}', lang={}, diff={}",
            request.getTopic(), request.getLanguage(), request.getDifficulty());

        try {
            if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Topic/prompt is required.");
                return ResponseEntity.badRequest().body(err);
            }
            if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
                request.setLanguage("Java");
            }
            if (request.getDifficulty() == null || request.getDifficulty().trim().isEmpty()) {
                request.setDifficulty("Medium");
            }
            if (request.getMarks() <= 0) {
                request.setMarks(10);
            }

            AiGenerateResponse result = aiQuestionService.generateQuestion(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Question generated successfully");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("AI generation failed", e);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "AI generation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }
}
