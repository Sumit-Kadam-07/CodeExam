package com.example.exam.dto;

import java.time.LocalDateTime;

public class LeaderboardEntry {
    private int rank;
    private Long resultId;
    private String studentName;
    private String studentUsername;
    private int scoreAchieved;
    private int totalMarks;
    private double percentage;
    private boolean fullyGraded;
    private LocalDateTime submissionTime;

    public LeaderboardEntry(int rank, Long resultId, String studentName, String studentUsername,
                            int scoreAchieved, int totalMarks, double percentage,
                            boolean fullyGraded, LocalDateTime submissionTime) {
        this.rank = rank;
        this.resultId = resultId;
        this.studentName = studentName;
        this.studentUsername = studentUsername;
        this.scoreAchieved = scoreAchieved;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.fullyGraded = fullyGraded;
        this.submissionTime = submissionTime;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public Long getResultId() { return resultId; }
    public String getStudentName() { return studentName; }
    public String getStudentUsername() { return studentUsername; }
    public int getScoreAchieved() { return scoreAchieved; }
    public int getTotalMarks() { return totalMarks; }
    public double getPercentage() { return percentage; }
    public boolean isFullyGraded() { return fullyGraded; }
    public LocalDateTime getSubmissionTime() { return submissionTime; }

    public String getInitials() {
        if (studentName == null || studentName.isBlank()) return "?";
        String[] parts = studentName.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return ("" + studentName.charAt(0)).toUpperCase();
    }

    public String getPercentageFormatted() {
        return String.format("%.1f", percentage);
    }
}
