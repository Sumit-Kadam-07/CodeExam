package com.example.exam.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.exam.dto.ApiResponse;
import com.example.exam.dto.CreateExamRequest;
import com.example.exam.dto.ExamDTO;
import com.example.exam.dto.QuestionDTO;
import com.example.exam.dto.UserDTO;
import com.example.exam.mapper.ExamMapper;
import com.example.exam.mapper.QuestionMapper;
import com.example.exam.mapper.UserMapper;
import com.example.exam.model.ActivityLog;
import com.example.exam.model.CodingSubmission;
import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.Question;
import com.example.exam.model.User;
import com.example.exam.repository.ActivityLogRepository;
import com.example.exam.repository.ExamRepository;
import com.example.exam.repository.ExamResultRepository;
import com.example.exam.repository.QuestionRepository;
import com.example.exam.repository.UserRepository;
import com.example.exam.service.AdminService;
import com.example.exam.service.LeaderboardService;
import com.example.exam.service.UserService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    private final AdminService adminService;
    private final LeaderboardService leaderboardService;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserService userService;

    public AdminApiController(AdminService adminService,
                               LeaderboardService leaderboardService,
                               ExamRepository examRepository,
                               ExamResultRepository examResultRepository,
                               QuestionRepository questionRepository,
                               UserRepository userRepository,
                               ActivityLogRepository activityLogRepository,
                               UserService userService) {
        this.adminService = adminService;
        this.leaderboardService = leaderboardService;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
        this.userService = userService;
    }

    @PostMapping("/students")
    public ResponseEntity<Map<String, Object>> createStudent(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");
        String fullName = (String) payload.getOrDefault("fullName", "");
        String email = (String) payload.getOrDefault("email", "");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "username and password required"));
        }

        com.example.exam.model.User u = new com.example.exam.model.User();
        u.setUsername(username);
        u.setPassword(password);
        u.setFullName(fullName);
        u.setEmail(email);
        try {
            userService.saveStudent(u);
            return ResponseEntity.ok(Map.of("success", true, "message", "Student created", "id", u.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════
    //  Dashboard
    // ═══════════════════════════════════════════════

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        logger.info("API: Admin dashboard request");
        AdminService.DashboardData data = adminService.prepareDashboard();

        Map<String, Object> response = new HashMap<>();
        response.put("totalStudents", data.totalStudents);
        response.put("totalExams", data.totalExams);
        response.put("totalQuestions", data.totalQuestions);
        response.put("totalSubmissions", data.totalSubmissions);
        response.put("chartLabels", data.chartLabels);
        response.put("chartData", data.chartData);

        List<Map<String, Object>> recentResults = new ArrayList<>();
        for (ExamResult r : data.recentResults) {
            Map<String, Object> rr = new HashMap<>();
            rr.put("id", r.getId());
            rr.put("examId", r.getExam() != null ? r.getExam().getId() : null);
            rr.put("studentName", r.getStudent() != null ? r.getStudent().getFullName() : "N/A");
            rr.put("examName", r.getExam() != null ? r.getExam().getTitle() : "N/A");
            rr.put("scoreAchieved", r.getScoreAchieved());
            rr.put("totalMarks", r.getTotalMarks());
            rr.put("submissionTime", r.getSubmissionTime() != null ? r.getSubmissionTime().toString() : null);
            recentResults.add(rr);
        }
        response.put("recentResults", recentResults);

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════
    //  Exams CRUD
    // ═══════════════════════════════════════════════

    @GetMapping("/exams")
    public ResponseEntity<List<ExamDTO>> getAllExams() {
        logger.info("API: Admin get all exams");
        List<Exam> exams = adminService.getAllExams();
        List<ExamDTO> dtos = exams.stream().map(ExamMapper::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/exams/{examId}")
    public ResponseEntity<ExamDTO> getExam(@PathVariable Long examId) {
        logger.info("API: Admin get exam {}", examId);
        Exam exam = adminService.loadExamForEdit(examId);
        return ResponseEntity.ok(ExamMapper.toDTO(exam));
    }

    @PostMapping("/exams")
    public ResponseEntity<ExamDTO> createExam(@Valid @RequestBody CreateExamRequest request) {
        logger.info("API: Admin create exam: title='{}'", request.getTitle());
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDurationInMinutes(request.getDurationInMinutes() != null ? request.getDurationInMinutes() : 60);
        adminService.addExam(exam);
        logger.info("API: Exam created with id={}", exam.getId());
        return ResponseEntity.ok(ExamMapper.toDTO(exam));
    }

    @PutMapping("/exams/{examId}")
    public ResponseEntity<ApiResponse<ExamDTO>> updateExam(@PathVariable Long examId,
                                                            @RequestBody Exam examData) {
        logger.info("API: Admin update exam {}: title='{}'", examId, examData.getTitle());
        adminService.updateExam(examId, examData);
        Exam updated = adminService.loadExamForEdit(examId);
        return ResponseEntity.ok(ApiResponse.success("Exam updated successfully", ExamMapper.toDTO(updated)));
    }

    @DeleteMapping("/exams/{examId}")
    public ResponseEntity<Map<String, Object>> deleteExam(@PathVariable Long examId) {
        logger.warn("API: Admin delete exam {}", examId);
        adminService.deleteExam(examId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Exam deleted successfully");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════
    //  Exam Publish / Unpublish
    // ═══════════════════════════════════════════════

    @PostMapping("/exams/{examId}/publish")
    public ResponseEntity<Map<String, Object>> publishExam(@PathVariable Long examId) {
        logger.info("API: Admin publish exam {}", examId);
        AdminService.PublishOutcome outcome = adminService.publishExam(examId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", outcome.success);
        response.put("message", outcome.success ? "Exam \"" + outcome.examTitle + "\" has been published!" : outcome.message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exams/{examId}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishExam(@PathVariable Long examId) {
        logger.info("API: Admin unpublish exam {}", examId);
        AdminService.PublishOutcome outcome = adminService.unpublishExam(examId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", outcome.success);
        response.put("message", "Exam \"" + outcome.examTitle + "\" has been unpublished.");
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════
    //  Questions CRUD
    // ═══════════════════════════════════════════════

    @GetMapping("/exams/{examId}/questions")
    public ResponseEntity<List<QuestionDTO>> getExamQuestions(@PathVariable Long examId) {
        logger.info("API: Admin get questions for exam {}", examId);
        Exam exam = adminService.loadExamForEdit(examId);
        List<QuestionDTO> dtos = exam.getQuestions().stream()
                .map(QuestionMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable Long questionId) {
        logger.info("API: Admin get question {}", questionId);
        Question question = adminService.loadQuestionForEdit(questionId);
        return ResponseEntity.ok(QuestionMapper.toDTO(question));
    }

    @PostMapping("/exams/{examId}/questions")
    @Transactional
    public ResponseEntity<Map<String, Object>> addQuestion(@PathVariable Long examId,
                                                             @RequestBody Map<String, Object> payload) {
        logger.info("API: Admin add question to exam {}", examId);

        String title = (String) payload.get("title");
        String problemStatement = (String) payload.get("problemStatement");
        String language = (String) payload.getOrDefault("language", "Java");
        String difficulty = (String) payload.getOrDefault("difficulty", "Medium");
        Integer marks = payload.get("marks") != null ? ((Number) payload.get("marks")).intValue() : 10;
        String starterCode = (String) payload.getOrDefault("starterCode", "");
        Integer executionTimeout = payload.get("executionTimeout") != null ? ((Number) payload.get("executionTimeout")).intValue() : 5000;
        Integer memoryLimit = payload.get("memoryLimit") != null ? ((Number) payload.get("memoryLimit")).intValue() : 256;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> testCases = (List<Map<String, Object>>) payload.get("testCases");

        Question question = new Question();
        question.setTitle(title);
        question.setProblemStatement(problemStatement);
        question.setLanguage(language);
        question.setDifficulty(difficulty);
        question.setMarks(marks);
        question.setStarterCode(starterCode);
        question.setExecutionTimeout(executionTimeout);
        question.setMemoryLimit(memoryLimit);
        question.setInputFormat((String) payload.getOrDefault("inputFormat", null));
        question.setOutputFormat((String) payload.getOrDefault("outputFormat", null));
        question.setConstraints((String) payload.getOrDefault("constraints", null));
        question.setSampleInput((String) payload.getOrDefault("sampleInput", null));
        question.setSampleOutput((String) payload.getOrDefault("sampleOutput", null));
        question.setExplanation((String) payload.getOrDefault("explanation", null));
        question.setExpectedSolution((String) payload.getOrDefault("expectedSolution", null));

        // Convert test cases to arrays for the existing service method
        String[] testCaseInputs = null;
        String[] testCaseExpectedOutputs = null;
        int[] testCaseWeights = null;
        String[] testCaseIsSample = null;

        if (testCases != null && !testCases.isEmpty()) {
            testCaseInputs = new String[testCases.size()];
            testCaseExpectedOutputs = new String[testCases.size()];
            testCaseWeights = new int[testCases.size()];
            testCaseIsSample = new String[testCases.size()];

            for (int i = 0; i < testCases.size(); i++) {
                Map<String, Object> tc = testCases.get(i);
                testCaseInputs[i] = (String) tc.getOrDefault("input", "");
                // Accept either 'expectedOutput' (new) or 'expected' (legacy/frontend)
                Object expectedObj = tc.getOrDefault("expectedOutput", tc.get("expected"));
                testCaseExpectedOutputs[i] = expectedObj != null ? (String) expectedObj : "";
                testCaseWeights[i] = tc.get("weight") != null ? ((Number) tc.get("weight")).intValue() : 1;
                testCaseIsSample[i] = tc.get("sample") != null && Boolean.TRUE.equals(tc.get("sample")) ? "true" : "false";
            }
        }

        String validationError = adminService.validateQuestionForSubmission(question, testCaseInputs, testCaseExpectedOutputs, testCaseWeights, testCaseIsSample);
        if (validationError != null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", validationError);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        AdminService.QuestionCreationOutcome outcome;
        try {
            outcome = adminService.addQuestionWithArrays(
                    question, examId, testCaseInputs, testCaseExpectedOutputs, testCaseWeights, testCaseIsSample);
        } catch (Exception e) {
            logger.error("Failed to add question to exam {}", examId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            return ResponseEntity.status(500).body(errorResponse);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", outcome.message);
        response.put("questionId", outcome.questionId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/questions/{questionId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateQuestion(@PathVariable Long questionId,
                                                               @RequestBody Map<String, Object> payload) {
        logger.info("API: Admin update question {}", questionId);

        Question existing = adminService.loadQuestionForEdit(questionId);

        String title = (String) payload.getOrDefault("title", existing.getTitle());
        String problemStatement = (String) payload.getOrDefault("problemStatement", existing.getProblemStatement());
        String language = (String) payload.getOrDefault("language", existing.getLanguage());
        String difficulty = (String) payload.getOrDefault("difficulty", existing.getDifficulty());
        Integer marks = payload.get("marks") != null ? ((Number) payload.get("marks")).intValue() : existing.getMarks();
        String starterCode = (String) payload.getOrDefault("starterCode", existing.getStarterCode());
        Integer executionTimeout = payload.get("executionTimeout") != null ? ((Number) payload.get("executionTimeout")).intValue() : existing.getExecutionTimeout();
        Integer memoryLimit = payload.get("memoryLimit") != null ? ((Number) payload.get("memoryLimit")).intValue() : existing.getMemoryLimit();

        existing.setTitle(title);
        existing.setProblemStatement(problemStatement);
        existing.setLanguage(language);
        existing.setDifficulty(difficulty);
        existing.setMarks(marks);
        existing.setStarterCode(starterCode);
        existing.setExecutionTimeout(executionTimeout);
        existing.setMemoryLimit(memoryLimit);
        existing.setInputFormat((String) payload.getOrDefault("inputFormat", existing.getInputFormat()));
        existing.setOutputFormat((String) payload.getOrDefault("outputFormat", existing.getOutputFormat()));
        existing.setConstraints((String) payload.getOrDefault("constraints", existing.getConstraints()));
        existing.setSampleInput((String) payload.getOrDefault("sampleInput", existing.getSampleInput()));
        existing.setSampleOutput((String) payload.getOrDefault("sampleOutput", existing.getSampleOutput()));
        existing.setExplanation((String) payload.getOrDefault("explanation", existing.getExplanation()));
        existing.setExpectedSolution((String) payload.getOrDefault("expectedSolution", existing.getExpectedSolution()));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> testCases = (List<Map<String, Object>>) payload.get("testCases");

        if (testCases != null) {
            String[] testCaseInputs = new String[testCases.size()];
            String[] testCaseExpectedOutputs = new String[testCases.size()];
            int[] testCaseWeights = new int[testCases.size()];
            String[] testCaseIsSample = new String[testCases.size()];

            for (int i = 0; i < testCases.size(); i++) {
                Map<String, Object> tc = testCases.get(i);
                testCaseInputs[i] = (String) tc.getOrDefault("input", "");
                // Accept either 'expectedOutput' or legacy 'expected'
                Object expectedObj = tc.getOrDefault("expectedOutput", tc.get("expected"));
                testCaseExpectedOutputs[i] = expectedObj != null ? (String) expectedObj : "";
                testCaseWeights[i] = tc.get("weight") != null ? ((Number) tc.get("weight")).intValue() : 1;
                testCaseIsSample[i] = tc.get("sample") != null && Boolean.TRUE.equals(tc.get("sample")) ? "true" : "false";
            }

            adminService.updateQuestionWithArrays(questionId, existing, testCaseInputs, testCaseExpectedOutputs, testCaseWeights, testCaseIsSample);
        } else {
            questionRepository.save(existing);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Question \"" + title + "\" updated successfully!");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(@PathVariable Long questionId,
                                                               @RequestParam(required = false) Long examId) {
        logger.warn("API: Admin delete question {}", questionId);
        // We need the examId, try to find it from the question
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null || question.getExam() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Question not found");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Long actualExamId = question.getExam().getId();
        AdminService.DeleteQuestionOutcome outcome = adminService.deleteQuestion(actualExamId, questionId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", !"error".equals(outcome.status));
        response.put("message", outcome.message);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/questions/{questionId}/testcases/{testCaseId}")
    public ResponseEntity<Map<String, Object>> deleteTestCase(@PathVariable Long questionId,
                                                               @PathVariable Long testCaseId) {
        logger.info("API: Admin delete testCase {} from question {}", testCaseId, questionId);
        AdminService.DeleteTestCaseOutcome outcome = adminService.deleteTestCase(questionId, testCaseId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", outcome.success);
        response.put("message", outcome.message);
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════
    //  Results & Grading
    // ═══════════════════════════════════════════════

    @GetMapping("/exams/{examId}/results")
    @Transactional
    public ResponseEntity<List<Map<String, Object>>> getExamResults(@PathVariable Long examId) {
        logger.info("API: Admin get results for exam {}", examId);
        AdminService.AdminResultsData data = adminService.viewResults(examId);
        List<Map<String, Object>> results = new ArrayList<>();
        for (ExamResult r : data.results) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("studentName", r.getStudent() != null ? r.getStudent().getFullName() : "N/A");
            map.put("examName", r.getExam() != null ? r.getExam().getTitle() : "N/A");
            map.put("scoreAchieved", r.getScoreAchieved());
            map.put("totalMarks", r.getTotalMarks());
            map.put("percentage", r.getTotalMarks() > 0 ? (r.getScoreAchieved() * 100.0 / r.getTotalMarks()) : 0.0);
            map.put("submissionTime", r.getSubmissionTime() != null ? r.getSubmissionTime().toString() : null);
            map.put("allReviewed", data.reviewedMap.getOrDefault(r.getId(), false));
            results.add(map);
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/results/{resultId}/grade")
    @Transactional
    public ResponseEntity<Map<String, Object>> getGradingData(@PathVariable Long resultId) {
        logger.info("API: Admin get grading data for result {}", resultId);
        AdminService.GradingPageData data = adminService.prepareGradingPage(resultId);

        Map<String, Object> response = new HashMap<>();
        response.put("resultId", data.result.getId());
        response.put("studentName", data.student.getFullName());
        response.put("studentUsername", data.student.getUsername());
        response.put("examTitle", data.exam.getTitle());
        response.put("examId", data.exam.getId());
        response.put("scoreAchieved", data.result.getScoreAchieved());
        response.put("totalMarks", data.result.getTotalMarks());
        response.put("submissionTime", data.result.getSubmissionTime() != null ? data.result.getSubmissionTime().toString() : null);

        List<Map<String, Object>> submissions = new ArrayList<>();
        for (CodingSubmission sub : data.submissions) {
            Map<String, Object> s = new HashMap<>();
            s.put("id", sub.getId());
            s.put("questionId", sub.getQuestion() != null ? sub.getQuestion().getId() : null);
            s.put("questionTitle", sub.getQuestion() != null ? sub.getQuestion().getTitle() : "N/A");
            s.put("language", sub.getLanguage());
            s.put("sourceCode", sub.getSourceCode());
            s.put("marksAwarded", sub.getMarksAwarded());
            s.put("maxMarks", sub.getQuestion() != null ? sub.getQuestion().getMarks() : 0);
            s.put("reviewed", sub.isReviewed());
            s.put("adminRemarks", sub.getAdminRemarks());
            s.put("status", sub.getStatus());
            s.put("executionTimeMs", sub.getExecutionTimeMs());
            s.put("compilationError", sub.getCompilationError());
            s.put("runtimeError", sub.getRuntimeError());
            s.put("passedTestCases", sub.getPassedTestCases());
            s.put("totalTestCases", sub.getTotalTestCases());
            submissions.add(s);
        }
        response.put("submissions", submissions);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/results/{resultId}/grade")
    @Transactional
    public ResponseEntity<Map<String, Object>> saveGrades(@PathVariable Long resultId,
                                                           @RequestBody Map<String, Object> payload) {
        logger.info("API: Admin save grades for result {}", resultId);

        @SuppressWarnings("unchecked")
        Map<String, Object> grades = (Map<String, Object>) payload.get("grades");

        Map<String, String> requestParams = new HashMap<>();
        if (grades != null) {
            for (Map.Entry<String, Object> entry : grades.entrySet()) {
                requestParams.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
            }
        }

        AdminService.SaveGradesOutcome outcome = adminService.saveGrades(resultId, requestParams);

        Map<String, Object> response = new HashMap<>();
        response.put("success", outcome.success);
        response.put("message", outcome.successMessage);
        response.put("examId", outcome.examId);
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════
    //  Students
    // ═══════════════════════════════════════════════

    @GetMapping("/students")
    public ResponseEntity<List<UserDTO>> getStudents() {
        logger.info("API: Admin get students list");
        List<User> students = adminService.listStudents();
        List<UserDTO> dtos = students.stream().map(UserMapper::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable Long studentId) {
        logger.warn("API: Admin delete student {}", studentId);
        AdminService.DeleteStudentOutcome outcome = adminService.deleteStudent(studentId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", outcome.success);
        response.put("message", outcome.message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/students/{userId}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long userId,
                                                              @RequestBody Map<String, String> payload) {
        logger.info("API: Admin reset password for user {}", userId);
        String newPassword = payload.get("newPassword");
        String result = adminService.resetStudentPassword(userId, newPassword);

        Map<String, Object> response = new HashMap<>();
        if (result.startsWith("SUCCESS:")) {
            response.put("success", true);
            response.put("message", result.substring("SUCCESS:".length()));
        } else {
            response.put("success", false);
            response.put("message", "Failed to reset password.");
        }
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════
    //  Activity Logs
    // ═══════════════════════════════════════════════

    @GetMapping("/activity-logs")
    public ResponseEntity<List<Map<String, Object>>> getActivityLogs() {
        logger.info("API: Admin get activity logs");
        List<ActivityLog> logs = activityLogRepository.findAllByOrderByTimestampDesc();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ActivityLog log : logs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("eventType", log.getEventType());
            map.put("details", log.getDetails());
            map.put("timestamp", log.getTimestamp() != null ? log.getTimestamp().toString() : null);
            map.put("studentName", log.getStudent() != null ? log.getStudent().getFullName() : "N/A");
            map.put("studentUsername", log.getStudent() != null ? log.getStudent().getUsername() : "N/A");
            map.put("examTitle", log.getExam() != null ? log.getExam().getTitle() : "N/A");
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // ═══════════════════════════════════════════════
    //  Leaderboard
    // ═══════════════════════════════════════════════

    @GetMapping("/leaderboard")
    @Transactional
    public ResponseEntity<Map<String, Object>> getGlobalLeaderboard() {
        logger.info("API: Admin get global leaderboard");
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
    @Transactional
    public ResponseEntity<Map<String, Object>> getExamLeaderboard(@PathVariable Long examId) {
        logger.info("API: Admin get leaderboard for exam {}", examId);
        LeaderboardService.ExamAdminLeaderboardData data = leaderboardService.adminExamLeaderboard(examId);

        Map<String, Object> response = new HashMap<>();
        response.put("exam", ExamMapper.toDTO(data.exam));
        response.put("entries", data.entries);
        response.put("topThree", data.topThree);
        response.put("avgScore", data.avgScore);
        response.put("avgPercentage", data.avgPercentage);
        response.put("highScore", data.highScore);
        response.put("passRate", data.passRate);
        response.put("gradedCount", data.gradedCount);
        response.put("pendingCount", data.pendingCount);
        response.put("totalCount", data.totalCount);
        response.put("dist", data.dist);
        return ResponseEntity.ok(response);
    }
}

