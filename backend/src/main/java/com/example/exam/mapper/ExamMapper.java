package com.example.exam.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.exam.dto.ExamDTO;
import com.example.exam.model.Exam;
import com.example.exam.model.Question;

public class ExamMapper {

    private ExamMapper() {
    }

    public static ExamDTO toDTO(Exam exam) {
        return toDTO(exam, true);
    }

    public static ExamDTO toDTO(Exam exam, boolean includeQuestions) {
        if (exam == null) {
            return null;
        }

        ExamDTO dto = new ExamDTO();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setDescription(exam.getDescription());
        dto.setDurationInMinutes(exam.getDurationInMinutes());
        dto.setDuration(exam.getDurationInMinutes());
        dto.setTotalMarks(exam.getTotalMarks());
        dto.setStatus(exam.isPublished() ? "PUBLISHED" : "DRAFT");
        dto.setPublished(exam.isPublished());

        if (includeQuestions) {
            List<Question> questions = exam.getQuestions();
            if (questions != null) {
                dto.setQuestions(
                    questions.stream()
                        .map(QuestionMapper::toDTO)
                        .collect(Collectors.toList())
                );
            } else {
                dto.setQuestions(Collections.emptyList());
            }
        } else {
            dto.setQuestions(Collections.emptyList());
        }

        if (exam.getCreatedAt() != null) {
            dto.setCreatedAt(exam.getCreatedAt().toString());
        }

        return dto;
    }
}
