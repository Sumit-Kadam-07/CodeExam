package com.example.exam.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exam.service.ExamService;

@RestController
@RequestMapping("/api/exam")
public class ExamApiController {

    private static final Logger logger = LoggerFactory.getLogger(ExamApiController.class);

    private final ExamService examService;

    public ExamApiController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitExam(@RequestBody Map<String, Object> payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Not authenticated"));
        }

        try {
            Number examIdNum = (Number) payload.get("examId");
            if (examIdNum == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing examId"));
            }
            Long examId = examIdNum.longValue();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answers = (List<Map<String, Object>>) payload.get("answers");

            Map<String, String> requestParams = new HashMap<>();
            if (answers != null) {
                for (Map<String, Object> a : answers) {
                    Number qn = (Number) a.get("questionId");
                    if (qn == null) continue;
                    String qid = String.valueOf(qn.longValue());
                    String source = a.getOrDefault("sourceCode", "").toString();
                    String language = a.getOrDefault("language", "Java").toString();
                    requestParams.put("sourceCode_" + qid, source);
                    requestParams.put("language_" + qid, language);
                }
            }

            ExamService.ExamSubmissionOutcome outcome = examService.submitExam(examId, requestParams, username);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", outcome.success);
            if (outcome.result != null) {
                resp.put("resultId", outcome.result.getId());
            }
            resp.put("message", outcome.success ? "Submitted" : "Failed");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            logger.error("Exam submit failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Server error: " + e.getMessage()));
        }
    }
}
