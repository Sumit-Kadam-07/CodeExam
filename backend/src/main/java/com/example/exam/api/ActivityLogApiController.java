package com.example.exam.api;

import java.util.HashMap;
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

import com.example.exam.model.ActivityLog;
import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.User;
import com.example.exam.repository.ExamRepository;
import com.example.exam.repository.ExamResultRepository;
import com.example.exam.repository.UserRepository;
import com.example.exam.service.ActivityLogService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/activity")
public class ActivityLogApiController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogApiController.class);

    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;

    public ActivityLogApiController(ActivityLogService activityLogService,
                                     UserRepository userRepository,
                                     ExamRepository examRepository,
                                     ExamResultRepository examResultRepository) {
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
    }

    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> logActivity(@RequestBody Map<String, Object> payload,
                                                            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            logger.debug("API: Activity log request from '{}'", username);

            User student = userRepository.findByUsername(username).orElse(null);
            if (student == null) {
                logger.warn("API: Activity log failed - student '{}' not found", username);
                response.put("success", false);
                response.put("error", "Student not found");
                return ResponseEntity.badRequest().body(response);
            }

            Long examId = payload.get("examId") != null ? Long.valueOf(payload.get("examId").toString()) : null;
            Long resultId = payload.get("resultId") != null ? Long.valueOf(payload.get("resultId").toString()) : null;
            String eventType = (String) payload.get("eventType");
            String details = (String) payload.get("details");

            if (eventType == null || eventType.isBlank()) {
                response.put("success", false);
                response.put("error", "eventType is required");
                return ResponseEntity.badRequest().body(response);
            }

            Exam exam = examId != null ? examRepository.findById(examId).orElse(null) : null;
            ExamResult examResult = resultId != null ? examResultRepository.findById(resultId).orElse(null) : null;

            ActivityLog log = activityLogService.logEvent(student, exam, examResult, eventType, details, request);

            logger.info("Activity logged: student='{}', eventType='{}', details='{}'", username, eventType, details);
            response.put("success", true);
            response.put("id", log.getId());
            response.put("timestamp", log.getTimestamp().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Activity log API error: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

