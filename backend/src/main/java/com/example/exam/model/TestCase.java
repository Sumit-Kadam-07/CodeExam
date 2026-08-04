package com.example.exam.model;

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
@Table(name = "test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Lob
    @Column(nullable = false)
    private String inputData;

    @Lob
    @Column(nullable = false)
    private String expectedOutput;

    @Column(nullable = false)
    private int weight = 1;

    @Column(name = "is_sample", nullable = false)
    private boolean isSample = false;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo = 0;
}
