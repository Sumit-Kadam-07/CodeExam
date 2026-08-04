 package com.example.exam.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.exam.model.CodingSubmission;
import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.Question;
import com.example.exam.model.TestCase;
import com.example.exam.model.User;
import com.example.exam.repository.ActivityLogRepository;
import com.example.exam.repository.CodingSubmissionRepository;
import com.example.exam.repository.ExamRepository;
import com.example.exam.repository.ExamResultRepository;
import com.example.exam.repository.QuestionRepository;
import com.example.exam.repository.TestCaseRepository;
import com.example.exam.repository.UserRepository;

@Service
public class AdminService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final TestCaseRepository testCaseRepository;
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodingSubmissionRepository codingSubmissionRepository;
    private final ActivityLogRepository activityLogRepository;

    public AdminService(ExamRepository examRepository,
                         QuestionRepository questionRepository,
                         TestCaseRepository testCaseRepository,
                         ExamResultRepository examResultRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         CodingSubmissionRepository codingSubmissionRepository,
                         ActivityLogRepository activityLogRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.testCaseRepository = testCaseRepository;
        this.examResultRepository = examResultRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.codingSubmissionRepository = codingSubmissionRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional(readOnly = true)
    public DashboardData prepareDashboard() {
        long totalStudents = userRepository.countByRole("ROLE_STUDENT");
        long totalExams = examRepository.count();
        long totalQuestions = questionRepository.count();
        long totalSubmissions = examResultRepository.count();

        // Use a single optimized query to get exam titles and submission counts
        List<Object[]> examStats = examRepository.getExamSubmissionCounts();
        List<String> chartLabels = new ArrayList<>();
        List<Integer> chartData = new ArrayList<>();
        for (Object[] row : examStats) {
            chartLabels.add((String) row[0]);
            chartData.add(((Number) row[1]).intValue());
        }

        List<ExamResult> recentResults = examResultRepository.findTop5ByOrderBySubmissionTimeDesc();

        return new DashboardData(
                totalStudents,
                totalExams,
                totalQuestions,
                totalSubmissions,
                chartLabels,
                chartData,
                recentResults
        );
    }

    @Transactional(readOnly = true)
    public List<Exam> getAllExams() {
        List<Exam> exams = examRepository.findAll();
        // Force initialization of lazy collections within the transaction
        for (Exam exam : exams) {
            exam.getQuestions().size();
            for (Question q : exam.getQuestions()) {
                q.getTestCases().size();
            }
        }
        return exams;
    }

    public void addExam(Exam exam) {
        exam.setPublished(false);
        exam.setCreatedAt(java.time.LocalDateTime.now());
        // Set createdBy from the currently authenticated admin user
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User admin = userRepository.findByUsername(username).orElse(null);
            exam.setCreatedBy(admin);
        }
        examRepository.save(exam);
    }

    @Transactional
    public PublishOutcome publishExam(Long examId) {
        Exam exam = getExamOrThrow(examId);

        // Force initialization of lazy questions collection
        List<Question> questions = exam.getQuestions();
        if (questions == null || questions.isEmpty()) {
            return new PublishOutcome(false, null, "Cannot publish an exam with no questions.");
        }

        // Initialize the collection and test cases
        questions.size();
        for (Question q : questions) {
            if (q.getTestCases() != null) {
                q.getTestCases().size();
            }
        }

        exam.setPublished(true);
        int total = questions.stream().mapToInt(Question::getMarks).sum();
        exam.setTotalMarks(total);
        examRepository.save(exam);

        return new PublishOutcome(true, exam.getTitle(), "success");
    }

@Transactional
public PublishOutcome unpublishExam(Long examId) {
        Exam exam = getExamOrThrow(examId);
        exam.setPublished(false);
        examRepository.save(exam);
        return new PublishOutcome(true, exam.getTitle(),
                "Exam \"" + exam.getTitle() + "\" has been unpublished.");
    }

    /**
     * Validate question fields before submission.
     * Returns error message if validation fails, null if ok.
     */
    public String validateQuestionForSubmission(Question question,
                                                  String[] testCaseInputs,
                                                  String[] testCaseExpectedOutputs,
                                                  int[] testCaseWeights) {
        return validateQuestionForSubmission(question, testCaseInputs, testCaseExpectedOutputs, testCaseWeights, null);
    }

    /**
     * Validate question fields before submission (with isSample support).
     * Returns error message if validation fails, null if ok.
     */
    public String validateQuestionForSubmission(Question question,
                                                  String[] testCaseInputs,
                                                  String[] testCaseExpectedOutputs,
                                                  int[] testCaseWeights,
                                                  String[] testCaseIsSample) {
        if (question == null) return "Question data is missing.";
        if (question.getTitle() == null || question.getTitle().isBlank()) return "Question title is required.";
        if (question.getProblemStatement() == null || question.getProblemStatement().isBlank()) return "Problem statement is required.";
        if (question.getLanguage() == null || question.getLanguage().isBlank()) question.setLanguage("Java");
        if (question.getMarks() <= 0) return "Marks must be greater than 0.";

        // Auto-generate starter code if empty
        if (question.getStarterCode() == null || question.getStarterCode().isBlank()) {
            question.setStarterCode(getDefaultStarterCode(question.getLanguage()));
        }

        // Default values
        if (question.getDifficulty() == null || question.getDifficulty().isBlank()) question.setDifficulty("Medium");
        if (question.getExecutionTimeout() <= 0) question.setExecutionTimeout(5000);

        if (testCaseInputs == null || testCaseInputs.length == 0) {
            return "At least one test case is required.";
        }
        boolean hasNonEmpty = false;
        for (int i = 0; i < testCaseInputs.length; i++) {
            String tc = testCaseInputs[i];
            if (tc != null && !tc.isBlank()) {
                hasNonEmpty = true;
                break;
            }
        }
        if (!hasNonEmpty) return "At least one test case with non-empty input is required.";
        return null;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public QuestionCreationOutcome addQuestion(Question question, Long examId, Map<String, Object> payload) {
        Exam exam = getExamOrThrow(examId);

        if (payload != null) {
            String[] testCaseInputs = (String[]) payload.get("testCaseInputs");
            String[] testCaseExpectedOutputs = (String[]) payload.get("testCaseExpectedOutputs");
            int[] testCaseWeights = (int[]) payload.get("testCaseWeights");
            String[] testCaseIsSample = (String[]) payload.get("testCaseIsSample");

            if (testCaseInputs != null && testCaseInputs.length > 0) {
                question.setExam(exam);
                if (question.getDifficulty() == null || question.getDifficulty().isBlank()) {
                    question.setDifficulty("Medium");
                }
                if (question.getLanguage() == null || question.getLanguage().isBlank()) {
                    question.setLanguage("Java");
                }
                if (question.getExecutionTimeout() <= 0) {
                    question.setExecutionTimeout(5000);
                }
                if (question.getTestCases() == null) {
                    question.setTestCases(new ArrayList<>());
                }

for (int i = 0; i < testCaseInputs.length; i++) {
                    String input = testCaseInputs[i];
                    String expected = (testCaseExpectedOutputs != null && i < testCaseExpectedOutputs.length) ? testCaseExpectedOutputs[i] : "";
                    if (input != null && !input.isBlank() && expected != null && !expected.isBlank()) {
                        TestCase testCase = new TestCase();
                        testCase.setQuestion(question);
                        testCase.setInputData(input);
                        testCase.setExpectedOutput(expected);
                        testCase.setWeight(testCaseWeights != null && testCaseWeights.length > i ? testCaseWeights[i] : 1);
                        testCase.setSample(testCaseIsSample != null && testCaseIsSample.length > i && "true".equals(testCaseIsSample[i]));
                        testCase.setSequenceNo(i);
                        question.getTestCases().add(testCase);
                    }
                }
                questionRepository.save(question);
            }
        } else {
            question.setExam(exam);
            if (question.getDifficulty() == null || question.getDifficulty().isBlank()) {
                question.setDifficulty("Medium");
            }
            if (question.getLanguage() == null || question.getLanguage().isBlank()) {
                question.setLanguage("Java");
            }
            if (question.getExecutionTimeout() <= 0) {
                question.setExecutionTimeout(5000);
            }
            questionRepository.save(question);
        }

        return new QuestionCreationOutcome(question.getId(), "Question \"" + question.getTitle() + "\" added successfully!");
    }

    @Transactional
    public QuestionCreationOutcome addQuestionWithArrays(Question question,
                                                            Long examId,
                                                            String[] testCaseInputs,
                                                            String[] testCaseExpectedOutputs,
                                                            int[] testCaseWeights,
                                                            String[] testCaseIsSample) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("testCaseInputs", testCaseInputs);
        payload.put("testCaseExpectedOutputs", testCaseExpectedOutputs);
        payload.put("testCaseWeights", testCaseWeights);
        payload.put("testCaseIsSample", testCaseIsSample);
        return addQuestion(question, examId, payload);
    }


    @Transactional
    public AdminResultsData viewResults(Long examId) {
        Exam exam = getExamOrThrow(examId);

        // Fetch results with coding submissions in a single query using JOIN FETCH
        List<ExamResult> results = examResultRepository.findByExamWithSubmissions(exam);
        Map<Long, Boolean> reviewedMap = new HashMap<>();
        for (ExamResult result : results) {
            // Submissions are already fetched via JOIN FETCH, initialize the collection
            result.getCodingSubmissions().size();
            boolean allReviewed = result.getCodingSubmissions().stream()
                    .allMatch(CodingSubmission::isReviewed);
            reviewedMap.put(result.getId(), allReviewed);
        }

        return new AdminResultsData(exam, results, reviewedMap);
    }


    @Transactional
    public GradingPageData prepareGradingPage(Long resultId) {
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid result Id:" + resultId));

        result.getCodingSubmissions().size();
        List<CodingSubmission> submissions = codingSubmissionRepository.findByExamResult(result);

        return new GradingPageData(result, submissions, result.getExam(), result.getStudent());
    }

    @Transactional
    public SaveGradesOutcome saveGrades(Long resultId, Map<String, String> requestParams) {
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid result Id:" + resultId));

        List<CodingSubmission> submissions = codingSubmissionRepository.findByExamResult(result);
        int totalScore = 0;

        for (CodingSubmission sub : submissions) {
            String markKey = "marks_" + sub.getId();
            String remarkKey = "remarks_" + sub.getId();

            if (requestParams.containsKey(markKey)) {
                try {
                    int marks = Integer.parseInt(requestParams.get(markKey).trim());
                    int maxMarks = sub.getQuestion().getMarks();

                    if (marks < 0) marks = 0;
                    if (marks > maxMarks) marks = maxMarks;

                    sub.setMarksAwarded(marks);
                    sub.setReviewed(true);
                    sub.setStatus("GRADED");
                    totalScore += marks;
                } catch (NumberFormatException e) {
                    sub.setMarksAwarded(0);
                    sub.setReviewed(true);
                    sub.setStatus("GRADED");
                }
            }

            if (requestParams.containsKey(remarkKey)) {
                sub.setAdminRemarks(requestParams.get(remarkKey));
            }

            codingSubmissionRepository.save(sub);
        }

        result.setScoreAchieved(totalScore);
        examResultRepository.save(result);

        String studentFullName = result.getStudent().getFullName();
        Long examIdResult = result.getExam().getId();

        return new SaveGradesOutcome(true,
                "Grades saved for " + studentFullName + ".",
                examIdResult);
    }


    @Transactional
    public void deleteExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id:" + examId));

        activityLogRepository.deleteByExam(exam);

        List<CodingSubmission> submissions = codingSubmissionRepository.findByExam(exam);
        if (!submissions.isEmpty()) {
            codingSubmissionRepository.deleteAll(submissions);
        }

        examRepository.delete(exam);
    }

    @Transactional(readOnly = true)
    public Exam loadExamForEdit(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id:" + examId));
        // Force initialization of lazy collections within the transaction
        exam.getQuestions().size();
        for (Question q : exam.getQuestions()) {
            q.getTestCases().size();
        }
        return exam;
    }

    @Transactional
    public void updateExam(Long examId, Exam updatedExam) {
        Exam existingExam = loadExamForEdit(examId);
        existingExam.setTitle(updatedExam.getTitle());
        existingExam.setDescription(updatedExam.getDescription());
        existingExam.setDurationInMinutes(updatedExam.getDurationInMinutes());
        examRepository.save(existingExam);
    }




    @Transactional
    public Question loadQuestionForEdit(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid question Id:" + questionId));

        question.getTestCases().size();
        return question;
    }

    @Transactional
    public Question updateQuestionWithArrays(Long questionId,
                                                 Question updatedQuestion,
                                                 String[] testCaseInputs,
                                                 String[] testCaseExpectedOutputs,
                                                 int[] testCaseWeights,
                                                 String[] testCaseIsSample) {

        return updateQuestion(
                questionId,
                updatedQuestion,
                testCaseInputs,
                testCaseExpectedOutputs,
                testCaseWeights,
                testCaseIsSample
        );
    }

    @Transactional
    public DeleteTestCaseOutcome deleteTestCase(Long questionId, Long testCaseId) {
        try {
            testCaseRepository.deleteById(testCaseId);
            return new DeleteTestCaseOutcome(true, "Test case removed successfully.", null);
        } catch (Exception e) {
            return new DeleteTestCaseOutcome(false, "Unable to remove test case.", e.getMessage());
        }
    }

    @Transactional
    public Question updateQuestion(Long questionId,
                                      Question updatedQuestion,
                                      String[] testCaseInputs,
                                      String[] testCaseExpectedOutputs,
                                      int[] testCaseWeights,
                                      String[] testCaseIsSample) {

        Question existingQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid question Id:" + questionId));

        existingQuestion.setTitle(updatedQuestion.getTitle());
        existingQuestion.setProblemStatement(updatedQuestion.getProblemStatement());
        existingQuestion.setInputFormat(updatedQuestion.getInputFormat());
        existingQuestion.setOutputFormat(updatedQuestion.getOutputFormat());
        existingQuestion.setConstraints(updatedQuestion.getConstraints());
        existingQuestion.setSampleInput(updatedQuestion.getSampleInput());
        existingQuestion.setSampleOutput(updatedQuestion.getSampleOutput());
        existingQuestion.setExplanation(updatedQuestion.getExplanation());
        existingQuestion.setExpectedSolution(updatedQuestion.getExpectedSolution());
        existingQuestion.setMarks(updatedQuestion.getMarks());
        existingQuestion.setDifficulty(updatedQuestion.getDifficulty());
        existingQuestion.setLanguage(updatedQuestion.getLanguage());
        existingQuestion.setStarterCode(updatedQuestion.getStarterCode());
        existingQuestion.setExecutionTimeout(updatedQuestion.getExecutionTimeout());
        existingQuestion.setMemoryLimit(updatedQuestion.getMemoryLimit());

        existingQuestion.getTestCases().clear();

if (testCaseInputs != null && testCaseExpectedOutputs != null && testCaseInputs.length > 0) {
            for (int i = 0; i < testCaseInputs.length; i++) {
                String input = testCaseInputs[i];
                String expected = (testCaseExpectedOutputs != null && i < testCaseExpectedOutputs.length) ? testCaseExpectedOutputs[i] : "";
                if (input != null && !input.isBlank() && expected != null && !expected.isBlank()) {
                    TestCase testCase = new TestCase();
                    testCase.setQuestion(existingQuestion);
                    testCase.setInputData(input);
                    testCase.setExpectedOutput(expected);
                    testCase.setWeight(testCaseWeights != null && testCaseWeights.length > i ? testCaseWeights[i] : 1);
                    testCase.setSample(testCaseIsSample != null && testCaseIsSample.length > i && "true".equals(testCaseIsSample[i]));
                    testCase.setSequenceNo(i);
                    existingQuestion.getTestCases().add(testCase);
                }
            }
        }

        questionRepository.save(existingQuestion);
        return existingQuestion;
    }

    @Transactional
    public DeleteQuestionOutcome deleteQuestion(Long examId, Long questionId) {
        try {
            Question question = questionRepository.findById(questionId).orElse(null);
            if (question != null) {
                List<CodingSubmission> submissions = codingSubmissionRepository.findByQuestion(question);
                if (!submissions.isEmpty()) {
                    codingSubmissionRepository.deleteAll(submissions);
                }
            }
            questionRepository.deleteById(questionId);
            return new DeleteQuestionOutcome("success", "Question deleted successfully.");
        } catch (DataIntegrityViolationException e) {
            return new DeleteQuestionOutcome("integrity", "Cannot delete this question. It has already been answered by students.");
        } catch (Exception e) {
            return new DeleteQuestionOutcome("error", "An unexpected error occurred while trying to delete the question.");
        }
    }

    @Transactional(readOnly = true)
    public List<User> listStudents() {
        return userRepository.findByRole("ROLE_STUDENT");
    }

    @Transactional
    public DeleteStudentOutcome deleteStudent(Long studentId) {
        try {
            Optional<User> studentOpt = userRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid student Id:" + studentId);
            }

            User student = studentOpt.get();

            activityLogRepository.deleteByStudent(student);

            List<CodingSubmission> submissions = codingSubmissionRepository.findByStudent(student);
            if (!submissions.isEmpty()) {
                codingSubmissionRepository.deleteAll(submissions);
            }

            examResultRepository.deleteByStudent(student);
            examResultRepository.flush();
            userRepository.delete(student);

            return new DeleteStudentOutcome(true,
                    "Student account and all associated results have been deleted.");
        } catch (DataIntegrityViolationException e) {
            return new DeleteStudentOutcome(false,
                    "Could not delete student. A database integrity error occurred.");
        } catch (Exception e) {
            return new DeleteStudentOutcome(false,
                    "An error occurred while trying to delete the student account: " + e.getMessage());
        }
    }

    public String resetStudentPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            return "__ERROR_EMPTY_PASSWORD__";
        }

        try {
            User student = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + userId));

            student.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(student);

            return "SUCCESS:" + "Password for " + student.getUsername() + " has been reset.";
        } catch (Exception e) {
            return "__ERROR_RESET__";
        }
    }

    public static String getDefaultStarterCode(String language) {
        if (language == null) return "";
        return switch (language.trim()) {
            case "C" -> "#include <stdio.h>\n\nint main() {\n\n    return 0;\n}";
            case "C++" -> "#include <iostream>\nusing namespace std;\n\nint main() {\n\n    return 0;\n}";
            case "Java" -> "import java.util.*;\n\npublic class Main {\n\n    public static void main(String[] args) {\n\n    }\n\n}";
            case "Python" -> "def main():\n    pass\n\nif __name__ == \"__main__\":\n    main()";
            default -> "";
        };
    }

    private Exam getExamOrThrow(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id:" + examId));
    }

    public static class DashboardData {
        public final long totalStudents;
        public final long totalExams;
        public final long totalQuestions;
        public final long totalSubmissions;
        public final List<String> chartLabels;
        public final List<Integer> chartData;
        public final List<ExamResult> recentResults;

        public DashboardData(long totalStudents,
                              long totalExams,
                              long totalQuestions,
                              long totalSubmissions,
                              List<String> chartLabels,
                              List<Integer> chartData,
                              List<ExamResult> recentResults) {
            this.totalStudents = totalStudents;
            this.totalExams = totalExams;
            this.totalQuestions = totalQuestions;
            this.totalSubmissions = totalSubmissions;
            this.chartLabels = chartLabels;
            this.chartData = chartData;
            this.recentResults = recentResults;
        }
    }

    public static class PublishOutcome {
        public final boolean success;
        public final String examTitle;
        public final String message;

        public PublishOutcome(boolean success, String examTitle, String message) {
            this.success = success;
            this.examTitle = examTitle;
            this.message = message;
        }
    }

    public static class QuestionCreationOutcome {
        public final Long questionId;
        public final String message;

        public QuestionCreationOutcome(Long questionId, String message) {
            this.questionId = questionId;
            this.message = message;
        }
    }

    public static class AdminResultsData {
        public final Exam exam;
        public final List<ExamResult> results;
        public final Map<Long, Boolean> reviewedMap;

        public AdminResultsData(Exam exam, List<ExamResult> results, Map<Long, Boolean> reviewedMap) {
            this.exam = exam;
            this.results = results;
            this.reviewedMap = reviewedMap;
        }
    }

    public static class GradingPageData {
        public final ExamResult result;
        public final List<CodingSubmission> submissions;
        public final Exam exam;
        public final User student;

        public GradingPageData(ExamResult result, List<CodingSubmission> submissions, Exam exam, User student) {
            this.result = result;
            this.submissions = submissions;
            this.exam = exam;
            this.student = student;
        }
    }

    public static class DeleteTestCaseOutcome {
        public final boolean success;
        public final String message;
        public final String details;

        public DeleteTestCaseOutcome(boolean success, String message, String details) {
            this.success = success;
            this.message = message;
            this.details = details;
        }
    }

    public static class DeleteQuestionOutcome {
        public final String status;
        public final String message;

        public DeleteQuestionOutcome(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    public static class DeleteStudentOutcome {
        public final boolean success;
        public final String message;

        public DeleteStudentOutcome(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class SaveGradesOutcome {
        public final boolean success;
        public final String successMessage;
        public final Long examId;

        public SaveGradesOutcome(boolean success, String successMessage, Long examId) {
            this.success = success;
            this.successMessage = successMessage;
            this.examId = examId;
        }
    }
}

