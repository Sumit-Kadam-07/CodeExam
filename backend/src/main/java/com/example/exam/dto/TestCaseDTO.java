package com.example.exam.dto;

public class TestCaseDTO {

    private Long id;
    private String inputData;
    private String expectedOutput;
    private Integer weight;
    private Boolean sample;

    public TestCaseDTO() {
    }

    public TestCaseDTO(Long id, String inputData, String expectedOutput, Integer weight, Boolean sample) {
        this.id = id;
        this.inputData = inputData;
        this.expectedOutput = expectedOutput;
        this.weight = weight;
        this.sample = sample;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Boolean getSample() {
        return sample;
    }

    public void setSample(Boolean sample) {
        this.sample = sample;
    }
}
