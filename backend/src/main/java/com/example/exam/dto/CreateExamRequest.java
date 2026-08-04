package com.example.exam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateExamRequest {
    @NotBlank(message = "Exam title is required")
    @Size(max = 200, message = "Exam title must not exceed 200 characters")
    private String title;

    @Size(max = 10000, message = "Exam description must not exceed 10,000 characters")
    private String description;

    @NotNull(message = "Exam duration is required")
    @Min(value = 1, message = "Exam duration must be at least 1 minute")
    private Integer durationInMinutes;

    public CreateExamRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(Integer durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }
}
