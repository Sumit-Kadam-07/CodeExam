package com.example.exam.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.exam.model.CodingSubmission;
import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.User;
import com.example.exam.repository.CodingSubmissionRepository;
import com.example.exam.repository.ExamRepository;
import com.example.exam.repository.ExamResultRepository;
import com.example.exam.repository.UserRepository;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final CodingSubmissionRepository codingSubmissionRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public StudentService(UserRepository userRepository,
                           ExamRepository examRepository,
                           ExamResultRepository examResultRepository,
                           CodingSubmissionRepository codingSubmissionRepository,
                           FileStorageService fileStorageService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.codingSubmissionRepository = codingSubmissionRepository;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    public static class DashboardData {
        public final com.example.exam.dto.UserDTO student;
        public final List<com.example.exam.dto.ResultDTO> pastResults;
        public final int totalTaken;
        public final double averageScore;
        public final int highestScore;
        public final List<String> chartLabels;
        public final List<Integer> chartData;
        public final List<com.example.exam.dto.ExamDTO> availableExams;

        public DashboardData(com.example.exam.dto.UserDTO student,
                              List<com.example.exam.dto.ResultDTO> pastResults,
                              int totalTaken,
                              double averageScore,
                              int highestScore,
                              List<String> chartLabels,
                              List<Integer> chartData,
                              List<com.example.exam.dto.ExamDTO> availableExams) {
            this.student = student;
            this.pastResults = pastResults;
            this.totalTaken = totalTaken;
            this.averageScore = averageScore;
            this.highestScore = highestScore;
            this.chartLabels = chartLabels;
            this.chartData = chartData;
            this.availableExams = availableExams;
        }
    }

    public static class ResultDetailData {
        public final ExamResult result;
        public final List<CodingSubmission> submissions;
        public final Exam exam;
        public final boolean allReviewed;

        public ResultDetailData(ExamResult result, List<CodingSubmission> submissions, Exam exam, boolean allReviewed) {
            this.result = result;
            this.submissions = submissions;
            this.exam = exam;
            this.allReviewed = allReviewed;
        }
    }

    public List<com.example.exam.dto.ResultDTO> getStudentResultDTOs() {
        User student = getAuthenticatedUser();
        List<ExamResult> pastResults = examResultRepository.findByStudentOrderBySubmissionTimeDesc(student);
        return pastResults.stream()
                .map(com.example.exam.mapper.ResultMapper::toDTO)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        String currentUsername = authentication.getName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public DashboardData prepareDashboard() {
        User student = getAuthenticatedUser();
        logger.debug("Preparing dashboard for student '{}'", student.getUsername());

        List<ExamResult> pastResults = examResultRepository.findByStudentOrderBySubmissionTimeDesc(student);

        int totalTaken = pastResults.size();
        double totalScore = 0;
        int totalPossible = 0;
        int highestScorePercent = 0;

        for (ExamResult result : pastResults) {
            totalScore += result.getScoreAchieved();
            totalPossible += result.getTotalMarks();
            if (result.getTotalMarks() > 0) {
                int percent = (int) ((result.getScoreAchieved() * 100.0) / result.getTotalMarks());
                if (percent > highestScorePercent) highestScorePercent = percent;
            }
        }

        double averageScore = (totalPossible > 0) ? (totalScore * 100.0) / totalPossible : 0;

        List<ExamResult> chartResults = new ArrayList<>(pastResults);
        Collections.reverse(chartResults);

        List<String> chartLabels = chartResults.stream()
                .map(r -> r.getExam().getTitle())
                .collect(Collectors.toList());
        List<Integer> chartData = chartResults.stream()
                .map(r -> r.getTotalMarks() > 0 ? (int) ((r.getScoreAchieved() * 100.0) / r.getTotalMarks()) : 0)
                .collect(Collectors.toList());

        List<Exam> allExams = examRepository.findAll();
        Set<Exam> takenExams = pastResults.stream().map(ExamResult::getExam).collect(Collectors.toSet());
        List<com.example.exam.dto.ExamDTO> availableExams = allExams.stream()
                .filter(exam -> {
                    exam.getQuestions().size();
                    for (var q : exam.getQuestions()) {
                        q.getTestCases().size();
                    }
                    return exam.isPublished() && !takenExams.contains(exam);
                })
                .map(com.example.exam.mapper.ExamMapper::toDTO)
                .collect(Collectors.toList());

        logger.debug("Dashboard prepared: {} exams taken, {} available", totalTaken, availableExams.size());
        return new DashboardData(
                com.example.exam.mapper.UserMapper.toDTO(student),
                pastResults.stream().map(com.example.exam.mapper.ResultMapper::toDTO).collect(Collectors.toList()),
                totalTaken,
                averageScore,
                highestScorePercent,
                chartLabels,
                chartData,
                availableExams
        );
    }

    @Transactional
    public ResultDetailData prepareResultDetail(Long resultId) {
        User student = getAuthenticatedUser();
        logger.debug("Preparing result detail for resultId={}, student='{}'", resultId, student.getUsername());
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid result Id:" + resultId));

        if (!result.getStudent().getId().equals(student.getId())) {
            logger.warn("Unauthorized result detail access: resultId={} by student '{}'", resultId, student.getUsername());
            return null;
        }

        List<CodingSubmission> submissions = codingSubmissionRepository.findByExamResult(result);
        boolean allReviewed = !submissions.isEmpty() && submissions.stream().allMatch(CodingSubmission::isReviewed);

        return new ResultDetailData(result, submissions, result.getExam(), allReviewed);
    }

    @Transactional
    public String updateProfilePicture(MultipartFile profilePicFile) {
        if (profilePicFile.isEmpty()) {
            logger.warn("Profile picture upload failed: empty file");
            return "Please select a file to upload.";
        }
        try {
            User student = getAuthenticatedUser();
            String filePath = fileStorageService.saveFile(profilePicFile);
            student.setProfilePicUrl(filePath);
            userRepository.save(student);
            logger.info("Profile picture updated for student '{}': {}", student.getUsername(), filePath);
            return "success";
        } catch (Exception e) {
            logger.error("Profile picture upload error for student: {}", e.getMessage());
            return "Error uploading file: " + e.getMessage();
        }
    }

    @Transactional
    public void updateStudentProfile(String username, String fullName, String mobileNumber) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (fullName != null && !fullName.isBlank()) {
            student.setFullName(fullName);
        }
        if (mobileNumber != null) {
            student.setMobileNumber(mobileNumber);
        }
        userRepository.save(student);
        logger.info("Profile updated for student '{}'", username);
    }

    @Transactional
    public String changeStudentPassword(String username, String oldPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            logger.warn("Password change failed: new passwords do not match for user '{}'", username);
            return "New passwords do not match.";
        }
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            logger.warn("Password change failed: incorrect old password for user '{}'", username);
            return "Incorrect old password.";
        }
        student.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(student);
        logger.info("Password changed successfully for student '{}'", username);
        return "SUCCESS:Password updated successfully.";
    }
}

