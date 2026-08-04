package com.example.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.User;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    List<ExamResult> findByExam(Exam exam);

    @Query("SELECT er FROM ExamResult er LEFT JOIN FETCH er.codingSubmissions WHERE er.exam = :exam")
    List<ExamResult> findByExamWithSubmissions(@Param("exam") Exam exam);

    List<ExamResult> findByStudent(User student);
    List<ExamResult> findByStudentOrderBySubmissionTimeDesc(User student);

    @Query("SELECT er FROM ExamResult er LEFT JOIN FETCH er.codingSubmissions WHERE er.student = :student ORDER BY er.submissionTime DESC")
    List<ExamResult> findByStudentWithSubmissions(@Param("student") User student);

    List<ExamResult> findTop5ByOrderBySubmissionTimeDesc();
    List<ExamResult> findByStudentAndExam(User student, Exam exam);

    @Transactional
    void deleteByStudent(User student);

    @Query("SELECT er FROM ExamResult er LEFT JOIN FETCH er.codingSubmissions LEFT JOIN FETCH er.exam LEFT JOIN FETCH er.student")
    List<ExamResult> findAllWithSubmissionsAndExamAndStudent();
}

