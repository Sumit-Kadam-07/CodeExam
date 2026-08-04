package com.example.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exam.model.ActivityLog;
import com.example.exam.model.Exam;
import com.example.exam.model.ExamResult;
import com.example.exam.model.User;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByExamResultOrderByTimestampDesc(ExamResult examResult);
    List<ActivityLog> findByStudentAndExamOrderByTimestampDesc(User student, Exam exam);
    List<ActivityLog> findByExamOrderByTimestampDesc(Exam exam);
    long countByExamResultAndEventType(ExamResult examResult, String eventType);
    List<ActivityLog> findAllByOrderByTimestampDesc();
    void deleteByExam(Exam exam);
    void deleteByStudent(User student);
}

