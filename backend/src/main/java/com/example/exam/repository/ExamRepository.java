package com.example.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.exam.model.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT e.title, COUNT(r.id) FROM Exam e LEFT JOIN e.results r GROUP BY e.id, e.title")
    java.util.List<Object[]> getExamSubmissionCounts();
}
