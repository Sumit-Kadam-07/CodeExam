package com.example.exam.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.exam.dto.CodeSubmissionDTO;
import com.example.exam.dto.ResultDTO;
import com.example.exam.model.CodingSubmission;
import com.example.exam.model.ExamResult;

public class ResultMapper {

    private ResultMapper() {
    }

    public static ResultDTO toDTO(ExamResult result) {
        if (result == null) {
            return null;
        }

        ResultDTO dto = new ResultDTO();
        dto.setId(result.getId());

        if (result.getExam() != null) {
            dto.setExamName(result.getExam().getTitle());
            dto.setExam(ExamMapper.toDTO(result.getExam(), false));
        }

        if (result.getStudent() != null) {
            dto.setStudentName(result.getStudent().getFullName());
        }

        dto.setSubmissionTime(result.getSubmissionTime());
        dto.setScoreAchieved(result.getScoreAchieved());
        dto.setTotalMarks(result.getTotalMarks());
        dto.setScore((double) result.getScoreAchieved());

        if (result.getTotalMarks() > 0) {
            dto.setPercentage(result.getScoreAchieved() * 100.0 / result.getTotalMarks());
        } else {
            dto.setPercentage(0.0);
        }

        dto.setStatus(result.getTotalMarks() > 0 ? "GRADED" : "PENDING");

        // Map submissions
        List<CodingSubmission> submissions = result.getCodingSubmissions();
        if (submissions != null) {
            dto.setSubmissions(
                submissions.stream()
                    .map(ResultMapper::toCodeSubmissionDTO)
                    .collect(Collectors.toList())
            );
            boolean allReviewed = submissions.stream().allMatch(CodingSubmission::isReviewed);
            dto.setAllReviewed(allReviewed);
        } else {
            dto.setSubmissions(Collections.emptyList());
            dto.setAllReviewed(false);
        }

        return dto;
    }

    private static CodeSubmissionDTO toCodeSubmissionDTO(CodingSubmission sub) {
        if (sub == null) return null;
        CodeSubmissionDTO dto = new CodeSubmissionDTO();
        dto.setId(sub.getId());
        if (sub.getQuestion() != null) {
            dto.setQuestion(QuestionMapper.toDTO(sub.getQuestion()));
        }
        dto.setSourceCode(sub.getSourceCode());
        dto.setLanguage(sub.getLanguage());
        dto.setMarksAwarded(sub.getMarksAwarded());
        dto.setAdminRemarks(sub.getAdminRemarks());
        dto.setReviewed(sub.isReviewed());
        dto.setStatus(sub.getStatus());
        dto.setExecutionTimeMs(sub.getExecutionTimeMs());
        dto.setCompilationError(sub.getCompilationError());
        dto.setRuntimeError(sub.getRuntimeError());
        dto.setPassedTestCases(sub.getPassedTestCases());
        dto.setTotalTestCases(sub.getTotalTestCases());
        return dto;
    }
}
