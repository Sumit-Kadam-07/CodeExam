package com.example.exam.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.exam.model.CodingSubmission;
import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.Question;
import com.example.exam.model.User;
import com.example.exam.repository.CodingSubmissionRepository;
import com.example.exam.repository.ExamRepository;
import com.example.exam.repository.ExamResultRepository;
import com.example.exam.repository.QuestionRepository;
import com.example.exam.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamService.class);

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final CodingSubmissionRepository codingSubmissionRepository;
    private final CodingEvaluationService codingEvaluationService;
    private final ObjectMapper objectMapper;

    public ExamService(
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            UserRepository userRepository,
            QuestionRepository questionRepository,
            CodingSubmissionRepository codingSubmissionRepository,
            CodingEvaluationService codingEvaluationService,
            ObjectMapper objectMapper) {
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.codingSubmissionRepository = codingSubmissionRepository;
        this.codingEvaluationService = codingEvaluationService;
        this.objectMapper = objectMapper;
    }

    public static class ExamPageData {
        public final User student;
        public final Exam exam;
        public final List<Question> questions;
        public boolean notPublished = false;
        public boolean alreadyTaken = false;

        public ExamPageData(User student, Exam exam, List<Question> questions) {
            this.student = student;
            this.exam = exam;
            this.questions = questions;
        }
    }

    @Transactional
    public ExamPageData getExamPageData(Long examId, String currentUsername) {
        User student = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id:" + examId));

        if (!exam.isPublished()) {
            ExamPageData sentinel = new ExamPageData(student, exam, null);
            sentinel.notPublished = true;
            return sentinel;
        }

        List<ExamResult> existingResults = examResultRepository.findByStudentAndExam(student, exam);
        if (!existingResults.isEmpty()) {
            ExamPageData sentinel = new ExamPageData(student, exam, null);
            sentinel.alreadyTaken = true;
            return sentinel;
        }

        List<Question> questions = exam.getQuestions();
        questions.forEach(q -> q.getTestCases().size());

        return new ExamPageData(student, exam, questions);
    }

    @Transactional
    public synchronized ExamSubmissionOutcome submitExam(Long examId, Map<String, String> requestParams, String currentUsername) {
        User student = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id:" + examId));

        List<ExamResult> existingResults = examResultRepository.findByStudentAndExam(student, exam);
        if (!existingResults.isEmpty()) {
            return new ExamSubmissionOutcome(false, "redirect:/student/dashboard", null);
        }

        // Ensure questions & testcases are initialized within transaction
        exam.getQuestions().forEach(q -> q.getTestCases().size());

        ExamResult result = new ExamResult();
        result.setStudent(student);
        result.setExam(exam);
        result.setSubmissionTime(LocalDateTime.now());
        result.setScoreAchieved(0);

        int totalMarks = exam.getQuestions().stream().mapToInt(Question::getMarks).sum();
        result.setTotalMarks(totalMarks);
        examResultRepository.save(result);

        int totalScore = 0;

        for (Question question : exam.getQuestions()) {
            // Use the question's execution timeout, with sensible bounds
            long perTestCaseTimeoutMs = Math.max(1000, Math.min(60000,
                    question.getExecutionTimeout() > 0 ? question.getExecutionTimeout() : 5000));

            String sourceCode = requestParams.getOrDefault("sourceCode_" + question.getId(), "");
            String language = requestParams.getOrDefault("language_" + question.getId(), "Java");

            String output = requestParams.getOrDefault("output_" + question.getId(), null);
            String lastSavedStr = requestParams.getOrDefault("lastSaved_" + question.getId(), null);

            CodingSubmission submission = new CodingSubmission();
            submission.setExamResult(result);
            submission.setExam(exam);
            submission.setStudent(student);
            submission.setQuestion(question);
            submission.setSourceCode(sourceCode);
            submission.setLanguage(language);
            submission.setSubmissionTime(LocalDateTime.now());
            submission.setMarksAwarded(0);
            submission.setReviewed(true);
            submission.setStatus("AUTO_GRADED");

            submission.setOutput(output);
            if (lastSavedStr != null && !lastSavedStr.isBlank()) {
                try {
                    submission.setLastSaved(LocalDateTime.parse(lastSavedStr));
                } catch (Exception ignore) {
                    submission.setLastSaved(LocalDateTime.now());
                }
            }

            // Universal auto-evaluation for all supported languages (C, C++, Java, Python)
            // Wrapped in try-catch to prevent one failed evaluation from crashing entire exam submission
            try {
                CodingEvaluationService.EvaluationResult eval = codingEvaluationService.evaluate(
                        language,
                        sourceCode,
                        question.getTestCases(),
                        perTestCaseTimeoutMs
                );

                long execMs = eval.getTotalExecutionTimeMs();
                String compilationError = eval.getCompilationError();
                String runtimeError = eval.getRuntimeError();

                submission.setExecutionTimeMs(execMs);
                submission.setCompilationError(compilationError);
                submission.setCompilationOutput(compilationError != null ? compilationError : "");
                submission.setRuntimeError(runtimeError);
                List<CodingEvaluationService.TestCaseResultItem> tcResults = eval.getTestCaseResults();
                submission.setTotalTestCases(tcResults.size());
                submission.setPassedTestCases((int) tcResults.stream()
                        .filter(CodingEvaluationService.TestCaseResultItem::isPassed).count());
                submission.setAutoEvaluated(true);

                // Persist test case results as JSON for detailed review
                try {
                    String tcResultsJson = objectMapper.writeValueAsString(
                        tcResults.stream()
                            .map(tc -> Map.of(
                                "index", tc.getTestCaseIndex(),
                                "input", tc.getInput(),
                                "expected", tc.getExpectedOutput(),
                                "actual", tc.getActualOutput(),
                                "passed", tc.isPassed(),
                                "visibleToStudent", tc.isVisibleToStudent(),
                                "timeMs", tc.getExecutionTimeMs(),
                                "memoryKb", tc.getMemoryKb(),
                                "status", tc.getStatus()
                            ))
                            .collect(Collectors.toList())
                    );
                    submission.setTestCaseResults(tcResultsJson);
                } catch (Exception e) {
                    logger.warn("Failed to serialize test case results JSON for question {}: {}", question.getId(), e.getMessage());
                }

                if (!eval.isCompiled()) {
                    submission.setMarksAwarded(0);
                    submission.setAutoScore(0.0);
                    submission.setStatus("COMPILE_FAILED");
                } else {
                    // Use the scaled marks from CodingEvaluationService
                    // finalMarks = round((earnedWeight / totalWeight) * question.marks, 2)
                    double scaledMarks = CodingEvaluationService.computeFinalMarks(
                            eval.getEarnedWeight(), eval.getTotalWeight(), question.getMarks());
                    int finalMarks = (int) Math.round(scaledMarks);
                    submission.setMarksAwarded(finalMarks);
                    submission.setAutoScore(scaledMarks);
                    submission.setTotalScore(scaledMarks);
                    submission.setStatus("AUTO_GRADED");
                }
            } catch (Exception e) {
                logger.error("Evaluation failed for question {} (examId={}): {}", question.getId(), examId, e.getMessage());
                submission.setStatus("EVALUATION_FAILED");
                submission.setMarksAwarded(0);
                submission.setAutoEvaluated(false);
                submission.setCompilationError("Evaluation error: " + e.getMessage());
            }

            totalScore += submission.getMarksAwarded();

            codingSubmissionRepository.save(submission);
        }

        int finalScore = codingSubmissionRepository.findByExamResult(result).stream()
                .mapToInt(CodingSubmission::getMarksAwarded)
                .sum();
        result.setScoreAchieved(finalScore);
        result.setTotalScore(finalScore);
        // Calculate percentage and pass status
        if (totalMarks > 0) {
            double pct = (finalScore * 100.0) / totalMarks;
            result.setPercentage(Math.round(pct * 100.0) / 100.0);
            result.setPass(pct >= 40.0);
        } else {
            result.setPercentage(0.0);
            result.setPass(false);
        }
        result.setFinishedAt(java.time.LocalDateTime.now());
        examResultRepository.save(result);

        return new ExamSubmissionOutcome(true, null, result);
    }

    public static class ExamSubmissionOutcome {
        public final boolean success;
        public final String redirectPath;
        public final ExamResult result;

        public ExamSubmissionOutcome(boolean success, String redirectPath, ExamResult result) {
            this.success = success;
            this.redirectPath = redirectPath;
            this.result = result;
        }
    }

    @Transactional(readOnly = true)
    public java.util.List<com.example.exam.dto.ExamDTO> getAvailableExamDTOs() {
        User student = userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        java.util.List<com.example.exam.model.ExamResult> pastResults = examResultRepository.findByStudentOrderBySubmissionTimeDesc(student);
        java.util.Set<com.example.exam.model.Exam> takenExams = pastResults.stream()
                .map(ExamResult::getExam)
                .collect(java.util.stream.Collectors.toSet());

        return examRepository.findAll().stream()
                .filter(exam -> {
                    // Force initialization of lazy collections within the transaction
                    exam.getQuestions().size();
                    for (Question q : exam.getQuestions()) {
                        q.getTestCases().size();
                    }
                    return exam.isPublished() && !takenExams.contains(exam);
                })
                .map(com.example.exam.mapper.ExamMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}

