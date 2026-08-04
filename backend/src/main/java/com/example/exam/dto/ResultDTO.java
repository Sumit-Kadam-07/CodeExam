package com.example.exam.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResultDTO {

    private Long id;
    private String examName;
    private String studentName;
    private Double score;
    private Double percentage;
    private LocalDateTime submissionTime;
    private Integer totalMarks;
    private Integer scoreAchieved;
    private String status;
    private ExamDTO exam;
    private List<CodeSubmissionDTO> submissions;
    private Boolean allReviewed;

    public ResultDTO() {
    }

    public ResultDTO(Long id,
                      String examName,
                      String studentName,
                      Double score,
                      Double percentage,
                      LocalDateTime submissionTime,
                      Integer totalMarks,
                      Integer scoreAchieved,
                      String status) {
        this.id = id;
        this.examName = examName;
        this.studentName = studentName;
        this.score = score;
        this.percentage = percentage;
        this.submissionTime = submissionTime;
        this.totalMarks = totalMarks;
        this.scoreAchieved = scoreAchieved;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(LocalDateTime submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Integer getScoreAchieved() {
        return scoreAchieved;
    }

    public void setScoreAchieved(Integer scoreAchieved) {
        this.scoreAchieved = scoreAchieved;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ExamDTO getExam() {
        return exam;
    }

    public void setExam(ExamDTO exam) {
        this.exam = exam;
    }

    public List<CodeSubmissionDTO> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<CodeSubmissionDTO> submissions) {
        this.submissions = submissions;
    }

    public Boolean getAllReviewed() {
        return allReviewed;
    }

    public void setAllReviewed(Boolean allReviewed) {
        this.allReviewed = allReviewed;
    }
}
