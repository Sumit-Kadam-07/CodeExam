package com.example.exam.dto;

public class GlobalLeaderboardEntry {
    private int rank;
    private Long studentId;
    private String studentName;
    private String studentUsername;
    private int totalScore;
    private int examsTaken;
    private int gradedExams;
    private double avgPercentage;

    public GlobalLeaderboardEntry(int rank, Long studentId, String studentName, String studentUsername,
                                   int totalScore, int examsTaken, int gradedExams, double avgPercentage) {
        this.rank = rank;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentUsername = studentUsername;
        this.totalScore = totalScore;
        this.examsTaken = examsTaken;
        this.gradedExams = gradedExams;
        this.avgPercentage = avgPercentage;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentUsername() { return studentUsername; }
    public int getTotalScore() { return totalScore; }
    public int getExamsTaken() { return examsTaken; }
    public int getGradedExams() { return gradedExams; }
    public double getAvgPercentage() { return avgPercentage; }

    public String getInitials() {
        if (studentName == null || studentName.isBlank()) return "?";
        String[] parts = studentName.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return ("" + studentName.charAt(0)).toUpperCase();
    }

    public String getAvgPercentageFormatted() {
        return String.format("%.1f", avgPercentage);
    }
}
