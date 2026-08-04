package com.example.exam.model;

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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank; // Import NotBlank
import lombok.Data;

@Data
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be empty")
    @Column(nullable = false, length = 500)
    private String title;

    @NotBlank(message = "Problem statement cannot be empty")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String problemStatement;

    @Lob
    private String inputFormat;

    @Lob
    private String outputFormat;

    @Lob
    private String constraints;

    @Lob
    private String sampleInput;

    @Lob
    private String sampleOutput;

    @Lob
    @Column(name = "explanation")
    private String explanation;

    @Column(name = "expected_solution", columnDefinition = "TEXT")
    private String expectedSolution;

    @Column(nullable = false)
    private int marks;

    @Column(nullable = false)
    private String difficulty = "Medium";

    @Column(nullable = false)
    private String language = "Java";

    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;

    @Column(name = "execution_timeout", nullable = false)
    private int executionTimeout = 5000;

    @Column(name = "memory_limit", nullable = true)
    private Integer memoryLimit; // in MB, nullable for future extension

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TestCase> testCases = new ArrayList<>();
}
