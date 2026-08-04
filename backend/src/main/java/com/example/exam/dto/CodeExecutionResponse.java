package com.example.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResponse {
    private boolean success;

    // Compilation stage
    private String compilationOutput;
    private String compilationError;

    // Runtime stage
    private String stdout;
    private String runtimeError;

    private Long executionTimeMs;

    public static CodeExecutionResponse compilationFailed(String compilationError) {
        CodeExecutionResponse r = new CodeExecutionResponse();
        r.setSuccess(false);
        r.setCompilationError(compilationError);
        r.setCompilationOutput(null);
        r.setStdout(null);
        r.setRuntimeError(null);
        r.setExecutionTimeMs(0L);
        return r;
    }
}

