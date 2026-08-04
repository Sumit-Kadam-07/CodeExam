package com.example.exam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exam.model.CodingSubmission;
import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.Question;
import com.example.exam.model.User;

@Repository
public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, Long> {
    List<CodingSubmission> findByExamResult(ExamResult examResult);
    List<CodingSubmission> findByExamResultIn(List<ExamResult> examResults);
    List<CodingSubmission> findByExam(Exam exam);
    List<CodingSubmission> findByQuestion(Question question);
    List<CodingSubmission> findByStudent(User student);
    Optional<CodingSubmission> findByExamResultAndQuestion(ExamResult examResult, Question question);
}
