package com.example.exam.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.exam.dto.ExamDTO;
import com.example.exam.dto.QuestionDTO;
import com.example.exam.dto.ResultDTO;
import com.example.exam.dto.UserDTO;
import com.example.exam.mapper.ExamMapper;
import com.example.exam.mapper.QuestionMapper;
import com.example.exam.mapper.ResultMapper;
import com.example.exam.mapper.UserMapper;
import com.example.exam.model.User;
import com.example.exam.service.ExamService;
import com.example.exam.service.LeaderboardService;
import com.example.exam.service.StudentService;
import com.example.exam.service.UserService;
import com.example.exam.util.SecurityUtils;

@RestController
@RequestMapping("/api/student")
public class StudentApiController {

    private static final Logger logger = LoggerFactory.getLogger(StudentApiController.class);

    private final StudentService studentService;
    private final UserService userService;
    private final ExamService examService;
    private final LeaderboardService leaderboardService;

    public StudentApiController(StudentService studentService,
                                 UserService userService,
                                 ExamService examService,
                                 LeaderboardService leaderboardService) {
        this.studentService = studentService;
        this.userService = userService;
        this.examService = examService;
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/exams/{examId}/questions")
    public ResponseEntity<?> getExamQuestionsForStudent(@PathVariable Long examId) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        ExamService.ExamPageData data = examService.getExamPageData(examId, username);
        if (data.notPublished) {
            return ResponseEntity.status(403).body(Map.of("error", "Exam not published"));
        }
        if (data.alreadyTaken) {
            return ResponseEntity.status(400).body(Map.of("error", "Exam already taken"));
        }

        List<QuestionDTO> dtos = data.questions.stream().map(QuestionMapper::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getStudentProfile() {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null || currentUsername.isBlank()) {
            logger.warn("API: Profile request with no valid authentication");
            return ResponseEntity.status(401).build();
        }
        logger.debug("API: Profile request for '{}'", currentUsername);
        return userService.findUserDTOByUsername(currentUsername)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("API: User '{}' not found", currentUsername);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getStudentDashboard() {
        logger.debug("API: Student dashboard request");
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).build();
        }
        
        StudentService.DashboardData data = studentService.prepareDashboard();
        
        Map<String, Object> response = new HashMap<>();
        response.put("student", data.student);
        response.put("pastResults", data.pastResults);
        response.put("totalTaken", data.totalTaken);
        response.put("averageScore", data.averageScore);
        response.put("highestScore", data.highestScore);
        response.put("chartLabels", data.chartLabels);
        response.put("chartData", data.chartData);
        response.put("availableExams", data.availableExams);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/results")
    public ResponseEntity<List<ResultDTO>> getStudentResults() {
        logger.debug("API: Results request");
        return ResponseEntity.ok(studentService.getStudentResultDTOs());
    }

    @GetMapping("/exams")
    public ResponseEntity<List<ExamDTO>> getAvailableExams() {
        logger.debug("API: Available exams request");
        return ResponseEntity.ok(examService.getAvailableExamDTOs());
    }
    
    @GetMapping("/result/{resultId}")
    public ResponseEntity<Map<String, Object>> getResultDetail(@PathVariable Long resultId) {
        logger.debug("API: Result detail request for resultId={}", resultId);
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).build();
        }
        
        StudentService.ResultDetailData data = studentService.prepareResultDetail(resultId);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("result", ResultMapper.toDTO(data.result));
        response.put("submissions", data.submissions.stream()
                .map(sub -> {
                    Map<String, Object> s = new HashMap<>();
                    s.put("id", sub.getId());
                    s.put("questionId", sub.getQuestion() != null ? sub.getQuestion().getId() : null);
                    s.put("questionTitle", sub.getQuestion() != null ? sub.getQuestion().getTitle() : "N/A");
                    s.put("sourceCode", sub.getSourceCode());
                    s.put("language", sub.getLanguage());
                    s.put("marksAwarded", sub.getMarksAwarded());
                    s.put("adminRemarks", sub.getAdminRemarks());
                    s.put("reviewed", sub.isReviewed());
                    s.put("status", sub.getStatus());
                    s.put("executionTimeMs", sub.getExecutionTimeMs());
                    s.put("compilationError", sub.getCompilationError());
                    s.put("runtimeError", sub.getRuntimeError());
                    s.put("passedTestCases", sub.getPassedTestCases());
                    s.put("totalTestCases", sub.getTotalTestCases());
                    s.put("testCaseResults", sub.getTestCaseResults());
                    return s;
                })
                .collect(Collectors.toList()));
        response.put("exam", ExamMapper.toDTO(data.exam, false));
        response.put("allReviewed", data.allReviewed);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getGlobalLeaderboard() {
        logger.debug("API: Student global leaderboard request");
        LeaderboardService.GlobalLeaderboardData data = leaderboardService.globalLeaderboard();
        
        Map<String, Object> response = new HashMap<>();
        response.put("entries", data.entries);
        response.put("topThree", data.topThree);
        response.put("totalStudents", data.totalStudents);
        response.put("totalExams", data.totalExams);
        response.put("publishedExams", data.publishedExams);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/leaderboard/{examId}")
    public ResponseEntity<Map<String, Object>> getExamLeaderboard(@PathVariable Long examId) {
        logger.debug("API: Student exam leaderboard request for examId={}", examId);
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).build();
        }
        
        LeaderboardService.ExamLeaderboardData data = leaderboardService.studentExamLeaderboard(examId, currentUsername);
        
        Map<String, Object> response = new HashMap<>();
        response.put("exam", data.exam);
        response.put("entries", data.entries);
        response.put("topThree", data.topThree);
        response.put("myEntry", data.myEntry);
        response.put("gradedCount", data.gradedCount);
        response.put("totalCount", data.totalCount);
        response.put("avgPercentage", data.avgPercentage);
        response.put("passRate", data.passRate);
        response.put("dist", data.dist);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> payload) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        String fullName = payload.get("fullName");
        String mobileNumber = payload.get("mobileNumber");
        try {
            studentService.updateStudentProfile(username, fullName, mobileNumber);
            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> payload) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        String confirmPassword = payload.get("confirmPassword");
        try {
            String result = studentService.changeStudentPassword(username, oldPassword, newPassword, confirmPassword);
            if (result.startsWith("SUCCESS:")) {
                return ResponseEntity.ok(Map.of("success", true, "message", result.substring("SUCCESS:".length())));
            }
            return ResponseEntity.status(400).body(Map.of("success", false, "message", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/profile/picture")
    public ResponseEntity<Map<String, Object>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Not authenticated"));
        }
        try {
            String result = studentService.updateProfilePicture(file);
            if ("success".equals(result)) {
                User user = userService.findByUsername(username);
                String profilePicUrl = user != null ? user.getProfilePicUrl() : null;
                return ResponseEntity.ok(Map.of("success", true, "message", "Profile picture updated", "profilePicUrl", profilePicUrl != null ? profilePicUrl : ""));
            }
            return ResponseEntity.status(400).body(Map.of("success", false, "message", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}