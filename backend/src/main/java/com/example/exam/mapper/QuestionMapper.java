package com.example.exam.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.exam.dto.QuestionDTO;
import com.example.exam.dto.TestCaseDTO;
import com.example.exam.model.Question;
import com.example.exam.model.TestCase;

public class QuestionMapper {

    private QuestionMapper() {
    }

    public static QuestionDTO toDTO(Question question) {
        if (question == null) {
            return null;
        }

        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setProblemStatement(question.getProblemStatement());
        dto.setInputFormat(question.getInputFormat());
        dto.setOutputFormat(question.getOutputFormat());
        dto.setConstraints(question.getConstraints());
        dto.setSampleInput(question.getSampleInput());
        dto.setSampleOutput(question.getSampleOutput());
        dto.setExplanation(question.getExplanation());
        dto.setExpectedSolution(question.getExpectedSolution());
        dto.setMarks(question.getMarks());
        dto.setDifficulty(question.getDifficulty());
        dto.setLanguage(question.getLanguage());
        dto.setStarterCode(question.getStarterCode());
        dto.setExecutionTimeout(question.getExecutionTimeout());
        dto.setMemoryLimit(question.getMemoryLimit());

        List<TestCase> testCases = question.getTestCases();
        if (testCases != null) {
            dto.setTestCases(
                testCases.stream()
                    .map(QuestionMapper::toTestCaseDTO)
                    .collect(Collectors.toList())
            );
        } else {
            dto.setTestCases(Collections.emptyList());
        }

        return dto;
    }

    private static TestCaseDTO toTestCaseDTO(TestCase tc) {
        if (tc == null) return null;
        TestCaseDTO dto = new TestCaseDTO();
        dto.setId(tc.getId());
        dto.setInputData(tc.getInputData());
        dto.setExpectedOutput(tc.getExpectedOutput());
        dto.setWeight(tc.getWeight());
        dto.setSample(tc.isSample());
        return dto;
    }
}
