
package com.example.exam.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "coding_submissions")
public class CodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id", nullable = false)
    private ExamResult examResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Lob
    @Column(nullable = false)
    private String sourceCode;

    @Column(nullable = false)
    private String language;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submissionTime;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo = 1;

    private LocalDateTime lastSaved;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String output;

    @Column(nullable = false)
    private int marksAwarded = -1;

    // --- Auto-evaluation fields ---
    private Long executionTimeMs;

    @Column(length = 10000)
    private String compilationError;

    @Column(length = 5000)
    private String runtimeError;

    @Column(name = "compilation_output", columnDefinition = "TEXT")
    private String compilationOutput;

    private Integer passedTestCases;
    private Integer totalTestCases;
    private Boolean autoEvaluated = false;
    private Double autoScore;

    @Column(name = "memory_kb")
    private Long memoryKb;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "test_case_results", columnDefinition = "TEXT")
    private String testCaseResults; // JSON string of test case results (expected vs actual)

    @Lob
    private String adminRemarks;

    @Column(nullable = false)
    private boolean reviewed = false;

    @Column(name = "manual_override", nullable = false)
    private boolean manualOverride = false;

    @Column(nullable = false, length = 20)
    private String status = "SUBMITTED";
}

