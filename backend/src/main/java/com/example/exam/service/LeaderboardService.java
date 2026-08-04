package com.example.exam.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.exam.dto.GlobalLeaderboardEntry;
import com.example.exam.dto.LeaderboardEntry;
import com.example.exam.model.CodingSubmission;
import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.User;
import com.example.exam.repository.ExamRepository;
import com.example.exam.repository.ExamResultRepository;
import com.example.exam.repository.UserRepository;

@Service
public class LeaderboardService {

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;

    public LeaderboardService(ExamRepository examRepository,
                               ExamResultRepository examResultRepository,
                               UserRepository userRepository) {
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.userRepository = userRepository;
    }

    public static class ExamLeaderboardData {
        public final Exam exam;
        public final List<LeaderboardEntry> entries;
        public final List<LeaderboardEntry> topThree;
        public final LeaderboardEntry myEntry;
        public final String currentUsername;
        public final int gradedCount;
        public final int totalCount;
        public final String avgPercentage;
        public final int passRate;
        public final List<Integer> dist;

        public ExamLeaderboardData(Exam exam,
                                    List<LeaderboardEntry> entries,
                                    List<LeaderboardEntry> topThree,
                                    LeaderboardEntry myEntry,
                                    String currentUsername,
                                    int gradedCount,
                                    int totalCount,
                                    String avgPercentage,
                                    int passRate,
                                    List<Integer> dist) {
            this.exam = exam;
            this.entries = entries;
            this.topThree = topThree;
            this.myEntry = myEntry;
            this.currentUsername = currentUsername;
            this.gradedCount = gradedCount;
            this.totalCount = totalCount;
            this.avgPercentage = avgPercentage;
            this.passRate = passRate;
            this.dist = dist;
        }
    }

    public static class ExamAdminLeaderboardData {
        public final Exam exam;
        public final List<LeaderboardEntry> entries;
        public final List<LeaderboardEntry> topThree;
        public final String avgScore;
        public final String avgPercentage;
        public final int highScore;
        public final int passRate;
        public final int gradedCount;
        public final int pendingCount;
        public final int totalCount;
        public final List<Integer> dist;

        public ExamAdminLeaderboardData(Exam exam,
                                         List<LeaderboardEntry> entries,
                                         List<LeaderboardEntry> topThree,
                                         String avgScore,
                                         String avgPercentage,
                                         int highScore,
                                         int passRate,
                                         int gradedCount,
                                         int pendingCount,
                                         int totalCount,
                                         List<Integer> dist) {
            this.exam = exam;
            this.entries = entries;
            this.topThree = topThree;
            this.avgScore = avgScore;
            this.avgPercentage = avgPercentage;
            this.highScore = highScore;
            this.passRate = passRate;
            this.gradedCount = gradedCount;
            this.pendingCount = pendingCount;
            this.totalCount = totalCount;
            this.dist = dist;
        }
    }

    public static class GlobalLeaderboardData {
        public final List<GlobalLeaderboardEntry> entries;
        public final List<GlobalLeaderboardEntry> topThree;
        public final int totalStudents;
        public final long totalExams;
        public final long publishedExams;

        public GlobalLeaderboardData(List<GlobalLeaderboardEntry> entries,
                                      List<GlobalLeaderboardEntry> topThree,
                                      int totalStudents,
                                      long totalExams,
                                      long publishedExams) {
            this.entries = entries;
            this.topThree = topThree;
            this.totalStudents = totalStudents;
            this.totalExams = totalExams;
            this.publishedExams = publishedExams;
        }
    }

    @Transactional(readOnly = true)
    public ExamAdminLeaderboardData adminExamLeaderboard(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id: " + examId));

        List<ExamResult> results = examResultRepository.findByExamWithSubmissions(exam);
        int effectiveTotal = exam.getTotalMarks() > 0 ? exam.getTotalMarks() : 1;

        List<LeaderboardEntry> gradedEntries = new ArrayList<>();
        List<LeaderboardEntry> pendingEntries = new ArrayList<>();

        for (ExamResult result : results) {
            List<CodingSubmission> subs = result.getCodingSubmissions();
            boolean allGraded = !subs.isEmpty() && subs.stream().allMatch(CodingSubmission::isReviewed);
            double pct = allGraded ? (result.getScoreAchieved() * 100.0 / effectiveTotal) : 0;
            String name = (result.getStudent().getFullName() != null && !result.getStudent().getFullName().isBlank())
                    ? result.getStudent().getFullName()
                    : result.getStudent().getUsername();

            LeaderboardEntry entry = new LeaderboardEntry(
                    0,
                    result.getId(),
                    name,
                    result.getStudent().getUsername(),
                    result.getScoreAchieved(),
                    exam.getTotalMarks(),
                    pct,
                    allGraded,
                    result.getSubmissionTime());

            if (allGraded) gradedEntries.add(entry);
            else pendingEntries.add(entry);
        }

        gradedEntries.sort(Comparator.comparingInt(LeaderboardEntry::getScoreAchieved).reversed()
                .thenComparing(LeaderboardEntry::getSubmissionTime));

        for (int i = 0; i < gradedEntries.size(); i++) {
            gradedEntries.get(i).setRank(i + 1);
        }
        for (LeaderboardEntry e : pendingEntries) {
            e.setRank(-1);
        }

        List<LeaderboardEntry> allEntries = new ArrayList<>(gradedEntries);
        allEntries.addAll(pendingEntries);

        double avgScore = gradedEntries.stream().mapToInt(LeaderboardEntry::getScoreAchieved).average().orElse(0);
        double avgPct = gradedEntries.stream().mapToDouble(LeaderboardEntry::getPercentage).average().orElse(0);
        int highScore = gradedEntries.stream().mapToInt(LeaderboardEntry::getScoreAchieved).max().orElse(0);
        long passCount = gradedEntries.stream().filter(e -> e.getPercentage() >= 50).count();
        int passRate = gradedEntries.isEmpty() ? 0 : (int) Math.round(passCount * 100.0 / gradedEntries.size());

        List<Integer> dist = new ArrayList<>(List.of(0, 0, 0, 0, 0));
        for (LeaderboardEntry e : gradedEntries) {
            int bucket = Math.min((int) (e.getPercentage() / 20), 4);
            dist.set(bucket, dist.get(bucket) + 1);
        }

        List<LeaderboardEntry> topThree = new ArrayList<>();
        if (!gradedEntries.isEmpty()) topThree.add(gradedEntries.get(0));
        if (gradedEntries.size() >= 2) topThree.add(gradedEntries.get(1));
        if (gradedEntries.size() >= 3) topThree.add(gradedEntries.get(2));

        return new ExamAdminLeaderboardData(
                exam,
                allEntries,
                topThree,
                String.format("%.1f", avgScore),
                String.format("%.1f", avgPct),
                highScore,
                passRate,
                gradedEntries.size(),
                pendingEntries.size(),
                results.size(),
                dist
        );
    }

    @Transactional(readOnly = true)
    public ExamLeaderboardData studentExamLeaderboard(Long examId, String currentUsername) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id: " + examId));

        List<ExamResult> results = examResultRepository.findByExamWithSubmissions(exam);
        int effectiveTotal = exam.getTotalMarks() > 0 ? exam.getTotalMarks() : 1;

        List<LeaderboardEntry> gradedEntries = new ArrayList<>();
        List<LeaderboardEntry> pendingEntries = new ArrayList<>();

        for (ExamResult result : results) {
            List<CodingSubmission> subs = result.getCodingSubmissions();
            boolean allGraded = !subs.isEmpty() && subs.stream().allMatch(CodingSubmission::isReviewed);
            double pct = allGraded ? (result.getScoreAchieved() * 100.0 / effectiveTotal) : 0;
            String name = (result.getStudent().getFullName() != null && !result.getStudent().getFullName().isBlank())
                    ? result.getStudent().getFullName()
                    : result.getStudent().getUsername();

            LeaderboardEntry entry = new LeaderboardEntry(
                    0,
                    result.getId(),
                    name,
                    result.getStudent().getUsername(),
                    result.getScoreAchieved(),
                    exam.getTotalMarks(),
                    pct,
                    allGraded,
                    result.getSubmissionTime());

            if (allGraded) gradedEntries.add(entry);
            else pendingEntries.add(entry);
        }

        gradedEntries.sort(Comparator.comparingInt(LeaderboardEntry::getScoreAchieved).reversed()
                .thenComparing(LeaderboardEntry::getSubmissionTime));

        for (int i = 0; i < gradedEntries.size(); i++) gradedEntries.get(i).setRank(i + 1);
        for (LeaderboardEntry e : pendingEntries) e.setRank(-1);

        List<LeaderboardEntry> allEntries = new ArrayList<>(gradedEntries);
        allEntries.addAll(pendingEntries);

        LeaderboardEntry myEntry = allEntries.stream()
                .filter(e -> e.getStudentUsername().equals(currentUsername))
                .findFirst().orElse(null);

        List<LeaderboardEntry> topThree = new ArrayList<>();
        if (!gradedEntries.isEmpty()) topThree.add(gradedEntries.get(0));
        if (gradedEntries.size() >= 2) topThree.add(gradedEntries.get(1));
        if (gradedEntries.size() >= 3) topThree.add(gradedEntries.get(2));

        List<Integer> dist = new ArrayList<>(List.of(0, 0, 0, 0, 0));
        for (LeaderboardEntry e : gradedEntries) {
            int bucket = Math.min((int) (e.getPercentage() / 20), 4);
            dist.set(bucket, dist.get(bucket) + 1);
        }

        double avgPct = gradedEntries.stream().mapToDouble(LeaderboardEntry::getPercentage).average().orElse(0);
        int passRate = gradedEntries.isEmpty() ? 0 :
                (int) Math.round(gradedEntries.stream().filter(e -> e.getPercentage() >= 50).count() * 100.0 / gradedEntries.size());

        return new ExamLeaderboardData(
                exam,
                allEntries,
                topThree,
                myEntry,
                currentUsername,
                gradedEntries.size(),
                results.size(),
                String.format("%.1f", avgPct),
                passRate,
                dist
        );
    }

    @Transactional(readOnly = true)
    public GlobalLeaderboardData globalLeaderboard() {
        List<ExamResult> allResults = examResultRepository.findAllWithSubmissionsAndExamAndStudent();

        Map<User, List<ExamResult>> resultsByStudent = new LinkedHashMap<>();
        for (ExamResult er : allResults) {
            resultsByStudent.computeIfAbsent(er.getStudent(), k -> new ArrayList<>()).add(er);
        }

        List<GlobalLeaderboardEntry> entries = new ArrayList<>();

        for (Map.Entry<User, List<ExamResult>> entry : resultsByStudent.entrySet()) {
            User student = entry.getKey();
            List<ExamResult> studentResults = entry.getValue();

            int totalScore = 0;
            int totalPossible = 0;
            int gradedExams = 0;

            for (ExamResult result : studentResults) {
                List<CodingSubmission> subs = result.getCodingSubmissions();
                boolean allGraded = !subs.isEmpty() && subs.stream().allMatch(CodingSubmission::isReviewed);
                if (allGraded) {
                    totalScore += result.getScoreAchieved();
                    totalPossible += result.getExam().getTotalMarks();
                    gradedExams++;
                }
            }

            double avgPct = totalPossible > 0 ? (totalScore * 100.0 / totalPossible) : 0;
            String name = (student.getFullName() != null && !student.getFullName().isBlank())
                    ? student.getFullName()
                    : student.getUsername();

            entries.add(new GlobalLeaderboardEntry(
                    0,
                    student.getId(),
                    name,
                    student.getUsername(),
                    totalScore,
                    studentResults.size(),
                    gradedExams,
                    avgPct
            ));
        }

        entries.sort(Comparator.comparingInt(GlobalLeaderboardEntry::getTotalScore).reversed()
                .thenComparingDouble(GlobalLeaderboardEntry::getAvgPercentage).reversed());

        for (int i = 0; i < entries.size(); i++) entries.get(i).setRank(i + 1);

        List<GlobalLeaderboardEntry> topThree = new ArrayList<>();
        if (!entries.isEmpty()) topThree.add(entries.get(0));
        if (entries.size() >= 2) topThree.add(entries.get(1));
        if (entries.size() >= 3) topThree.add(entries.get(2));

        long totalExams = examRepository.count();
        long publishedExams = examRepository.findAll().stream().filter(Exam::isPublished).count();

        return new GlobalLeaderboardData(entries, topThree, resultsByStudent.size(), totalExams, publishedExams);
    }
}
