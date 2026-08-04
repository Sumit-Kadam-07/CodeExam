package com.example.exam.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.exam.model.ActivityLog;
import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.User;
import com.example.exam.repository.ActivityLogRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ActivityLogService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogService.class);

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public ActivityLog logEvent(User student, Exam exam, ExamResult examResult,
                                String eventType, String details,
                                HttpServletRequest request) {
        ActivityLog log = new ActivityLog(student, exam, examResult, eventType, details);
        log.setTimestamp(LocalDateTime.now());

        if (request != null) {
            log.setIpAddress(getClientIp(request));
            log.setBrowserInfo(request.getHeader("User-Agent"));
        }

        ActivityLog saved = activityLogRepository.save(log);
        logger.info("Activity logged: student='{}', eventType='{}', details='{}'",
                student != null ? student.getUsername() : "null", eventType, details);
        return saved;
    }

    @Transactional
    public ActivityLog logEvent(User student, Exam exam, ExamResult examResult,
                                String eventType, String details,
                                String ipAddress, String browserInfo) {
        ActivityLog log = new ActivityLog(student, exam, examResult, eventType, details);
        log.setTimestamp(LocalDateTime.now());
        log.setIpAddress(ipAddress);
        log.setBrowserInfo(browserInfo);
        ActivityLog saved = activityLogRepository.save(log);
        logger.info("Activity logged (with IP): student='{}', eventType='{}', ip='{}'",
                student != null ? student.getUsername() : "null", eventType, ipAddress);
        return saved;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "0.0.0.0";
    }
}

