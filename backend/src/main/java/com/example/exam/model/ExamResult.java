package com.example.exam.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "exam_results", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "exam_id", "attempt_no"})
})
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false)
    private int scoreAchieved;
    @Column(nullable = false)
    private int totalMarks;

    @Column(name = "total_score", nullable = false, columnDefinition = "int default 0")
    private int totalScore = 0;
    private LocalDateTime submissionTime;

    @Column(nullable = false, columnDefinition = "int default 1")
    private int attemptNo = 1;

    @Column(nullable = false, columnDefinition = "DECIMAL(6,2) default 0.00")
    private double percentage = 0.0;

    @Column(name = "is_pass", nullable = false, columnDefinition = "boolean default false")
    private boolean isPass = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "examResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<CodingSubmission> codingSubmissions = new ArrayList<>();

    public void addCodingSubmission(CodingSubmission submission) {
        codingSubmissions.add(submission);
        submission.setExamResult(this);
    }
}

