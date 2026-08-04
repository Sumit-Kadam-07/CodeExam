package com.example.exam.execution;

/**
 * Standard result returned by any {@link ExecutionProvider}.
 * <p>
 * Mirrors the fields of {@link com.example.exam.dto.CodeExecutionResponse}
 * to allow seamless translation in the service layer.
 * </p>
 */
public class ProviderResult {

    private final boolean success;
    private final boolean compiled;
    private final String compilationError;
    private final String compilationOutput;
    private final String stdout;
    private final String runtimeError;
    private final Long executionTimeMs;
    private final Long memoryKb;
    private final String status;
    private final boolean timedOut;

    // ── Private constructor (use static factories) ──

    private ProviderResult(boolean success, boolean compiled,
                           String compilationError, String compilationOutput,
                           String stdout, String runtimeError,
                           Long executionTimeMs, Long memoryKb,
                           String status, boolean timedOut) {
        this.success = success;
        this.compiled = compiled;
        this.compilationError = compilationError;
        this.compilationOutput = compilationOutput;
        this.stdout = stdout;
        this.runtimeError = runtimeError;
        this.executionTimeMs = executionTimeMs;
        this.memoryKb = memoryKb;
        this.status = status;
        this.timedOut = timedOut;
    }

    // ── Static factories ──

    /** Compilation succeeded (scripting languages / no-op). */
    public static ProviderResult compilationSucceeded() {
        return new ProviderResult(true, true, null, null, null, null, 0L, null, "COMPILED", false);
    }

    public static ProviderResult compilationSucceeded(long executionMs) {
        return new ProviderResult(true, true, null, null, null, null, executionMs, null, "COMPILED", false);
    }

    /** Compilation failed with an error message. */
    public static ProviderResult compileFailed(String compilationError) {
        return compileFailed(compilationError, 0L);
    }

    public static ProviderResult compileFailed(String compilationError, long executionMs) {
        return new ProviderResult(false, false, compilationError, null, null, null, executionMs, null, "COMPILE_FAILED", false);
    }

    /** Runtime execution produced output (possibly with errors). */
    public static ProviderResult executionSucceeded(String stdout, Long executionTimeMs, Long memoryKb) {
        return new ProviderResult(true, true, null, null, stdout, null, executionTimeMs, memoryKb, "ACCEPTED", false);
    }

    public static ProviderResult runtimeFailed(String runtimeError, String stdout, long executionMs) {
        return new ProviderResult(false, true, null, null, stdout, runtimeError, executionMs, null, "RUNTIME_ERROR", false);
    }

    public static ProviderResult timedOut(long executionMs) {
        return new ProviderResult(false, true, null, null, null, "Time Limit Exceeded", executionMs, null, "TIME_LIMIT_EXCEEDED", true);
    }

    public static ProviderResult compileThenRun(boolean compiled, String compilationError,
                                                  String stdout, String runtimeError,
                                                  Long executionTimeMs, Long memoryKb) {
        if (!compiled) {
            return new ProviderResult(false, false, compilationError, null, null, null,
                    executionTimeMs != null ? executionTimeMs : 0L, null, "COMPILE_FAILED", false);
        }
        boolean success = runtimeError == null || runtimeError.isBlank();
        String status = success ? "ACCEPTED" : "RUNTIME_ERROR";
        return new ProviderResult(success, true, null, null, stdout, runtimeError,
                executionTimeMs, memoryKb, status, false);
    }

    // ── Getters ──

    public boolean isSuccess() { return success; }
    public boolean isCompiled() { return compiled; }
    public String getCompilationError() { return compilationError; }
    public String getCompilationOutput() { return compilationOutput; }
    public String getStdout() { return stdout; }
    public String getRuntimeError() { return runtimeError; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public Long getMemoryKb() { return memoryKb; }
    public String getStatus() { return status; }
    public boolean isTimedOut() { return timedOut; }
}
