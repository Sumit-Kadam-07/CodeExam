package com.example.exam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.exam.execution.Judge0Provider;
import com.example.exam.execution.LocalCompilerProvider;
import com.example.exam.execution.ProviderResult;
import com.example.exam.util.ProcessExecutionUtils;
import com.example.exam.util.ProcessExecutionUtils.ExecResult;

/**
 * Service that compiles and executes code using the configured
 * {@link ExecutionProvider} chain.
 * <p>
 * Tries {@link Judge0Provider} first (if enabled and available).
 * Falls back to {@link LocalCompilerProvider} if Judge0 is unavailable.
 * </p>
 */
@Service
public class CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(CodeExecutionService.class);
    private static final long MAX_OUTPUT_CHARS = ProcessExecutionUtils.MAX_OUTPUT_CHARS;

    private final Judge0Provider judge0Provider;
    private final LocalCompilerProvider localProvider;

    public CodeExecutionService(Judge0Provider judge0Provider,
                                 LocalCompilerProvider localProvider) {
        this.judge0Provider = judge0Provider;
        this.localProvider = localProvider;
    }

    // ─────────────────────────────────────────────────
    //  Public API — uses provider chain
    // ─────────────────────────────────────────────────

    /**
     * Compile source code, trying Judge0 first, then local.
     */
    public InternalExecutionResult compile(String language, String sourceCode) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return InternalExecutionResult.compileFailed("Empty source code.");
        }

        // 1. Try Judge0
        ProviderResult result = judge0Provider.compile(language, sourceCode);
        if (result != null) {
            log.debug("Judge0 compile result for lang={}: success={}, compiled={}",
                    language, result.isSuccess(), result.isCompiled());
            return toInternal(result);
        }

        // 2. Fall back to local
        log.info("Falling back to local compiler for compile, lang={}", language);
        result = localProvider.compile(language, sourceCode);
        return toInternal(result);
    }

    /**
     * Execute (compile + run) source code with optional stdin.
     * Tries Judge0 first, then local.
     */
    public InternalExecutionResult run(String language, String sourceCode, String input) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return InternalExecutionResult.runtimeFailed("Empty source code.", null, 0);
        }

        // 1. Try Judge0
        ProviderResult result = judge0Provider.execute(language, sourceCode, input);
        if (result != null) {
            log.debug("Judge0 execute result for lang={}: success={}, status={}",
                    language, result.isSuccess(), result.getStatus());
            return toInternal(result);
        }

        // 2. Fall back to local
        log.info("Falling back to local compiler for execute, lang={}", language);
        result = localProvider.execute(language, sourceCode, input);
        return toInternal(result);
    }

    // ─────────────────────────────────────────────────
    //  ProviderResult → InternalExecutionResult bridge
    // ─────────────────────────────────────────────────

    private InternalExecutionResult toInternal(ProviderResult pr) {
        if (pr == null) {
            return InternalExecutionResult.compileFailed("No execution result from any provider.");
        }

        // Compilation succeeded (or scripting language with no compilation step)
        if (pr.isCompiled()) {
            if (pr.getCompilationError() != null && !pr.getCompilationError().isBlank()) {
                // Compilation errored
                return new InternalExecutionResult(
                        false, false,
                        pr.getCompilationError(), pr.getCompilationOutput() != null
                                ? pr.getCompilationOutput() : pr.getCompilationError(),
                        null, null, pr.getExecutionTimeMs());
            }
            // If stdout or runtimeError is present, this is a run result
            if (pr.getStdout() != null || pr.getRuntimeError() != null) {
                return new InternalExecutionResult(
                        pr.isSuccess(), true, null, null,
                        pr.getStdout(), pr.getRuntimeError(), pr.getExecutionTimeMs());
            }
            // Pure compile success
            return new InternalExecutionResult(
                    true, true, null, null, null, null, pr.getExecutionTimeMs());
        }

        // Not compiled (failed before compilation)
        return new InternalExecutionResult(
                false, false,
                pr.getCompilationError() != null ? pr.getCompilationError() : "Compilation error",
                pr.getCompilationOutput(), null, null, pr.getExecutionTimeMs());
    }

    // ─────────────────────────────────────────────────
    //  InternalExecutionResult (backward-compatible DTO)
    // ─────────────────────────────────────────────────

    /**
     * Backward-compatible result DTO used by {@link com.example.exam.controller.CodeExecutionController}.
     * <p>
     * Mirrors the original inner class structure so controllers need not change.
     * </p>
     */
    public static class InternalExecutionResult {
        private final boolean success;
        private final boolean compiled;
        private final String compilationError;
        private final String compilationOutput;
        private final String stdout;
        private final String runtimeError;
        private final Long executionTimeMs;

        private InternalExecutionResult(boolean success, boolean compiled,
                                         String compilationError, String compilationOutput,
                                         String stdout, String runtimeError,
                                         Long executionTimeMs) {
            this.success = success;
            this.compiled = compiled;
            this.compilationError = compilationError;
            this.compilationOutput = compilationOutput;
            this.stdout = stdout;
            this.runtimeError = runtimeError;
            this.executionTimeMs = executionTimeMs;
        }

        // ── Static factories ──

        public static InternalExecutionResult compilationSucceeded() {
            return new InternalExecutionResult(true, true, null, null, null, null, 0L);
        }

        public static InternalExecutionResult compilationSucceeded(long executionMs) {
            return new InternalExecutionResult(true, true, null, null, null, null, executionMs);
        }

        public static InternalExecutionResult compileFailed(String error) {
            return compileFailed(error, 0L);
        }

        public static InternalExecutionResult compileFailed(String error, long ms) {
            return new InternalExecutionResult(false, false, error, null, null, null, ms);
        }

        public static InternalExecutionResult runtimeFailed(String runtimeError, String stdout, long ms) {
            return new InternalExecutionResult(false, true, null, null, stdout, runtimeError, ms);
        }

        public static InternalExecutionResult fromRunOutput(ExecResult run, long executionTimeMs) {
            String out = ProcessExecutionUtils.trimTo(run.stdout, MAX_OUTPUT_CHARS);
            String err = ProcessExecutionUtils.trimTo(run.stderr, MAX_OUTPUT_CHARS);
            if (run.exitCode == 0) {
                return new InternalExecutionResult(true, true, null, null, out, null, executionTimeMs);
            }
            return new InternalExecutionResult(false, true, null, null, out,
                    err != null && !err.isBlank() ? err : "Runtime error", executionTimeMs);
        }

        // ── Getters ──

        public boolean isCompiled() { return compiled; }
        public String getCompilationError() { return compilationError; }
        public String getCompilationOutput() { return compilationOutput; }
        public String getStdout() { return stdout; }
        public String getRuntimeError() { return runtimeError; }
        public boolean isSuccess() { return success; }
        public Long getExecutionTimeMs() { return executionTimeMs; }
    }
}

