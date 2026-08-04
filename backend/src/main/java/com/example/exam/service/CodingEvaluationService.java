package com.example.exam.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.exam.model.TestCase;
import com.example.exam.util.ProcessExecutionUtils;
import com.example.exam.util.ProcessExecutionUtils.ExecResult;
import com.example.exam.util.SourceCodeUtils;

/**
 * Service for auto-evaluating student code submissions against test cases.
 * Supports C, C++, Java, and Python.
 *
 * Scoring Formula:
 *   normalizedActual = actual.trim().replace("\r\n", "\n")
 *   normalizedExpected = expected.trim().replace("\r\n", "\n")
 *   passed = normalizedActual.equals(normalizedExpected)
 *   earnedWeight = sum(tc.weight where passed)
 *   totalWeight = sum(tc.weight for ALL test cases)
 *   if totalWeight == 0 -> finalMarks = 0
 *   finalMarks = round((earnedWeight / totalWeight) * question.marks, 2 decimals)
 *
 * Hidden Test Case Protection:
 * If a test case is NOT a sample (i.e., is hidden), its actualOutput
 * is set to null and visibleToStudent is false in returned results.
 */
@Service
public class CodingEvaluationService {

    private static final long MAX_OUTPUT_CHARS = ProcessExecutionUtils.MAX_OUTPUT_CHARS;

    // ========================================================================
    //  Public API
    // ========================================================================

    /**
     * Evaluate a student's source code against the given test cases.
     */
    public EvaluationResult evaluate(String language,
                                     String sourceCode,
                                     List<TestCase> testCases,
                                     long perTestCaseTimeoutMs) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return EvaluationResult.compileFailed("Empty source code.", testCases);
        }

        String lang = SourceCodeUtils.normalize(language);
        return switch (lang) {
            case "c" -> evaluateC(sourceCode, testCases, perTestCaseTimeoutMs);
            case "cpp" -> evaluateCpp(sourceCode, testCases, perTestCaseTimeoutMs);
            case "java" -> evaluateJava(sourceCode, testCases, perTestCaseTimeoutMs);
            case "python" -> evaluatePython(sourceCode, testCases, perTestCaseTimeoutMs);
            default -> EvaluationResult.compileFailed("Unsupported language: " + language, testCases);
        };
    }

    // ========================================================================
    //  Language-specific evaluation
    // ========================================================================

    public EvaluationResult evaluateC(String sourceCode,
                                      List<TestCase> testCases,
                                      long perTestCaseTimeoutMs) {
        Path workDir = ProcessExecutionUtils.createTempDir("coding-exam-");
        if (workDir == null) {
            return EvaluationResult.compileFailed("Unable to create temp directory.", testCases);
        }
        try {
            Path srcFile = workDir.resolve("Main.c");
            Path outFile = workDir.resolve("a.out");
            Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);

            long compileStart = System.nanoTime();
            String compileError = compileC(srcFile, outFile);
            long compileTimeMs = ProcessExecutionUtils.toMs(System.nanoTime() - compileStart);

            if (compileError != null) {
                return EvaluationResult.compileFailed(compileError, testCases, compileTimeMs);
            }

            return runTestCasesAgainstBinary(outFile, testCases, perTestCaseTimeoutMs, workDir, compileTimeMs);
        } catch (IOException e) {
            return EvaluationResult.compileFailed("IO error: " + e.getMessage(), testCases);
        } finally {
            ProcessExecutionUtils.deleteRecursively(workDir.toFile());
        }
    }

    public EvaluationResult evaluateCpp(String sourceCode,
                                        List<TestCase> testCases,
                                        long perTestCaseTimeoutMs) {
        Path workDir = ProcessExecutionUtils.createTempDir("coding-exam-");
        if (workDir == null) {
            return EvaluationResult.compileFailed("Unable to create temp directory.", testCases);
        }
        try {
            Path srcFile = workDir.resolve("Main.cpp");
            Path outFile = workDir.resolve("a.out");
            Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);

            long compileStart = System.nanoTime();
            String compileError = compileCpp(srcFile, outFile);
            long compileTimeMs = ProcessExecutionUtils.toMs(System.nanoTime() - compileStart);

            if (compileError != null) {
                return EvaluationResult.compileFailed(compileError, testCases, compileTimeMs);
            }

            return runTestCasesAgainstBinary(outFile, testCases, perTestCaseTimeoutMs, workDir, compileTimeMs);
        } catch (IOException e) {
            return EvaluationResult.compileFailed("IO error: " + e.getMessage(), testCases);
        } finally {
            ProcessExecutionUtils.deleteRecursively(workDir.toFile());
        }
    }

    public EvaluationResult evaluateJava(String sourceCode,
                                         List<TestCase> testCases,
                                         long perTestCaseTimeoutMs) {
        Path workDir = ProcessExecutionUtils.createTempDir("coding-exam-");
        if (workDir == null) {
            return EvaluationResult.compileFailed("Unable to create temp directory.", testCases);
        }
        try {
            Path srcDir = workDir.resolve("src");
            Path outDir = workDir.resolve("out");
            Files.createDirectories(srcDir);
            Files.createDirectories(outDir);

            String className = SourceCodeUtils.extractPrimaryClassName(sourceCode);
            if (className == null) className = "Solution";

            Path javaFile = srcDir.resolve(className + ".java");
            Files.writeString(javaFile, sourceCode, StandardCharsets.UTF_8);

            long compileStart = System.nanoTime();
            String compileError = compileJava(srcDir, outDir, className);
            long compileTimeMs = ProcessExecutionUtils.toMs(System.nanoTime() - compileStart);

            if (compileError != null) {
                return EvaluationResult.compileFailed(compileError, testCases, compileTimeMs);
            }

            List<TestCaseResultItem> tcResults = new ArrayList<>();
            long totalExecMs = 0;
            long totalMemoryKb = 0;
            int earnedWeight = 0;
            int totalWeight = testCases.stream().mapToInt(tc -> Math.max(0, tc.getWeight())).sum();
            String runtimeError = null;

            int idx = 0;
            for (TestCase tc : testCases) {
                idx++;
                long start = System.nanoTime();
                RunOutput run = runJavaMain(outDir, className, tc.getInputData(), perTestCaseTimeoutMs);
                long execMs = ProcessExecutionUtils.toMs(System.nanoTime() - start);
                totalExecMs += execMs;

                if (!run.success && runtimeError == null) {
                    runtimeError = run.error;
                }

                String normalizedActual = normalizeOutput(run.output);
                String normalizedExpected = normalizeOutput(tc.getExpectedOutput());
                boolean passed = run.success && normalizedActual.equals(normalizedExpected);

                TestCaseResultItem result = new TestCaseResultItem();
                result.setTestCaseIndex(idx);
                result.setInput(tc.getInputData());
                result.setExpectedOutput(tc.getExpectedOutput());
                result.setPassed(passed);
                result.setExecutionTimeMs(execMs);
                result.setMemoryKb(0L);
                result.setStatus(passed ? "PASS" : (run.success ? "FAIL" : "RUNTIME_ERROR"));

                // Hidden test cases: hide actual output from student
                if (!tc.isSample()) {
                    result.setActualOutput(null);
                    result.setVisibleToStudent(false);
                } else {
                    result.setActualOutput(run.output);
                    result.setVisibleToStudent(true);
                }

                if (passed) {
                    earnedWeight += Math.max(0, tc.getWeight());
                }

                tcResults.add(result);
            }

            return new EvaluationResult(true, null, tcResults, runtimeError,
                    compileTimeMs + totalExecMs, totalMemoryKb, 0.0, earnedWeight, totalWeight);

        } catch (IOException e) {
            return EvaluationResult.compileFailed("IO error: " + e.getMessage(), testCases);
        } finally {
            ProcessExecutionUtils.deleteRecursively(workDir.toFile());
        }
    }

    public EvaluationResult evaluatePython(String sourceCode,
                                           List<TestCase> testCases,
                                           long perTestCaseTimeoutMs) {
        Path workDir = ProcessExecutionUtils.createTempDir("coding-exam-");
        if (workDir == null) {
            return EvaluationResult.compileFailed("Unable to create temp directory.", testCases);
        }
        try {
            Path srcFile = workDir.resolve("main.py");
            Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);
            return runTestCasesWithInterpreter("python", srcFile, testCases, perTestCaseTimeoutMs, workDir);
        } catch (IOException e) {
            return EvaluationResult.compileFailed("IO error: " + e.getMessage(), testCases);
        } finally {
            ProcessExecutionUtils.deleteRecursively(workDir.toFile());
        }
    }

    // ========================================================================
    //  Shared execution helpers
    // ========================================================================

    private EvaluationResult runTestCasesAgainstBinary(Path binaryPath,
                                                       List<TestCase> testCases,
                                                       long perTestCaseTimeoutMs,
                                                       Path workDir,
                                                       long compileTimeMs) {
        List<TestCaseResultItem> tcResults = new ArrayList<>();
        long totalExecMs = 0;
        long totalMemoryKb = 0;
        int earnedWeight = 0;
        int totalWeight = testCases.stream().mapToInt(tc -> Math.max(0, tc.getWeight())).sum();
        String runtimeError = null;

        int idx = 0;
        for (TestCase tc : testCases) {
            idx++;
            ProcessBuilder runPb = new ProcessBuilder(binaryPath.toAbsolutePath().toString());
            runPb.directory(workDir.toFile());

            long start = System.nanoTime();
            ExecResult run = ProcessExecutionUtils.executeWithTimeout(runPb, tc.getInputData() != null
                    ? tc.getInputData().getBytes(StandardCharsets.UTF_8) : new byte[0], perTestCaseTimeoutMs);
            long execMs = ProcessExecutionUtils.toMs(System.nanoTime() - start);
            totalExecMs += execMs;

            if (run.exitCode != 0 && runtimeError == null) {
                runtimeError = run.stderr != null && !run.stderr.isBlank() ? run.stderr : "Runtime error (exit code " + run.exitCode + ")";
            }

            String normalizedActual = normalizeOutput(run.stdout);
            String normalizedExpected = normalizeOutput(tc.getExpectedOutput());
            boolean passed = (run.exitCode == 0) && normalizedActual.equals(normalizedExpected);

            TestCaseResultItem result = new TestCaseResultItem();
            result.setTestCaseIndex(idx);
            result.setInput(tc.getInputData());
            result.setExpectedOutput(tc.getExpectedOutput());
            result.setPassed(passed);
            result.setExecutionTimeMs(execMs);
            result.setMemoryKb(0L);
            result.setStatus(passed ? "PASS" : (run.exitCode == 0 ? "FAIL" : "RUNTIME_ERROR"));

            if (!tc.isSample()) {
                result.setActualOutput(null);
                result.setVisibleToStudent(false);
            } else {
                result.setActualOutput(run.stdout);
                result.setVisibleToStudent(true);
            }

            if (passed) {
                earnedWeight += Math.max(0, tc.getWeight());
            }

            tcResults.add(result);
        }

        double finalMarks = computeFinalMarks(earnedWeight, totalWeight, 0);

        return new EvaluationResult(true, null, tcResults, runtimeError,
                compileTimeMs + totalExecMs, totalMemoryKb, finalMarks, earnedWeight, totalWeight);
    }

    private EvaluationResult runTestCasesWithInterpreter(String interpreter,
                                                          Path scriptFile,
                                                          List<TestCase> testCases,
                                                          long perTestCaseTimeoutMs,
                                                          Path workDir) {
        List<TestCaseResultItem> tcResults = new ArrayList<>();
        long totalExecMs = 0;
        long totalMemoryKb = 0;
        int earnedWeight = 0;
        int totalWeight = testCases.stream().mapToInt(tc -> Math.max(0, tc.getWeight())).sum();
        String runtimeError = null;

        int idx = 0;
        for (TestCase tc : testCases) {
            idx++;
            ProcessBuilder runPb = new ProcessBuilder(interpreter, scriptFile.toAbsolutePath().toString());
            runPb.directory(workDir.toFile());

            long start = System.nanoTime();
            ExecResult run = ProcessExecutionUtils.executeWithTimeout(runPb, tc.getInputData() != null
                    ? tc.getInputData().getBytes(StandardCharsets.UTF_8) : new byte[0], perTestCaseTimeoutMs);
            long execMs = ProcessExecutionUtils.toMs(System.nanoTime() - start);
            totalExecMs += execMs;

            if (run.exitCode != 0 && runtimeError == null) {
                runtimeError = run.stderr != null && !run.stderr.isBlank() ? run.stderr : "Runtime error (exit code " + run.exitCode + ")";
            }

            String normalizedActual = normalizeOutput(run.stdout);
            String normalizedExpected = normalizeOutput(tc.getExpectedOutput());
            boolean passed = (run.exitCode == 0) && normalizedActual.equals(normalizedExpected);

            TestCaseResultItem result = new TestCaseResultItem();
            result.setTestCaseIndex(idx);
            result.setInput(tc.getInputData());
            result.setExpectedOutput(tc.getExpectedOutput());
            result.setPassed(passed);
            result.setExecutionTimeMs(execMs);
            result.setMemoryKb(0L);
            result.setStatus(passed ? "PASS" : (run.exitCode == 0 ? "FAIL" : "RUNTIME_ERROR"));

            if (!tc.isSample()) {
                result.setActualOutput(null);
                result.setVisibleToStudent(false);
            } else {
                result.setActualOutput(run.stdout);
                result.setVisibleToStudent(true);
            }

            if (passed) {
                earnedWeight += Math.max(0, tc.getWeight());
            }

            tcResults.add(result);
        }

        double finalMarks = computeFinalMarks(earnedWeight, totalWeight, 0);

        return new EvaluationResult(true, null, tcResults, runtimeError,
                totalExecMs, totalMemoryKb, finalMarks, earnedWeight, totalWeight);
    }

    // ========================================================================
    //  Compilation helpers
    // ========================================================================

    private String compileC(Path srcFile, Path outFile) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("gcc",
                srcFile.toAbsolutePath().toString(),
                "-O2", "-pipe", "-o", outFile.toAbsolutePath().toString(),
                "-lm");
        ExecResult r = ProcessExecutionUtils.executeWithTimeout(pb, null, 10_000);
        if (r.exitCode != 0) {
            return r.combinedOutput;
        }
        return null;
    }

    private String compileCpp(Path srcFile, Path outFile) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("g++",
                srcFile.toAbsolutePath().toString(),
                "-O2", "-pipe", "-o", outFile.toAbsolutePath().toString(),
                "-lm");
        ExecResult r = ProcessExecutionUtils.executeWithTimeout(pb, null, 10_000);
        if (r.exitCode != 0) {
            return r.combinedOutput;
        }
        return null;
    }

    private String compileJava(Path srcDir, Path outDir, String className) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "javac", "-encoding", "UTF-8",
                "-d", outDir.toAbsolutePath().toString(),
                srcDir.resolve(className + ".java").toAbsolutePath().toString());
        ExecResult r = ProcessExecutionUtils.executeWithTimeout(pb, null, 10_000);
        if (r.exitCode != 0) {
            return r.combinedOutput;
        }
        return null;
    }

    // ========================================================================
    //  Java runtime helper
    // ========================================================================

    private static class RunOutput {
        private final boolean success;
        private final String output;
        private final String error;

        private RunOutput(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }
    }

    private RunOutput runJavaMain(Path outDir, String className, String input, long timeoutMs) {
        ProcessBuilder pb = new ProcessBuilder(
                "java", "-Xmx256m",
                "-cp", outDir.toAbsolutePath().toString(),
                className);
        pb.directory(outDir.toFile());

        ExecResult run = ProcessExecutionUtils.executeWithTimeout(pb,
                input != null ? input.getBytes(StandardCharsets.UTF_8) : new byte[0], timeoutMs);

        if (run.exitCode == 0) {
            return new RunOutput(true, ProcessExecutionUtils.trimTo(run.stdout, MAX_OUTPUT_CHARS), null);
        }
        if (run.exitCode == 124) {
            return new RunOutput(false, "", "Time Limit Exceeded after " + timeoutMs + "ms");
        }
        String err = run.stderr != null && !run.stderr.isBlank() ? run.stderr : "Runtime error";
        return new RunOutput(false, ProcessExecutionUtils.trimTo(run.stdout, MAX_OUTPUT_CHARS), err);
    }

    // ========================================================================
    //  Output normalization
    // ========================================================================

    /**
     * Normalize output for comparison:
     * actual.trim().replace("\r\n", "\n") vs expected.trim().replace("\r\n", "\n")
     */
    static String normalizeOutput(String s) {
        if (s == null) return "";
        return s.trim().replace("\r\n", "\n");
    }

    // ========================================================================
    //  Score calculation
    // ========================================================================

    /**
     * Compute final marks using the formula:
     *   if totalWeight == 0 -> 0.0
     *   finalMarks = round((earnedWeight / totalWeight) * questionMarks, 2 decimals)
     *
     * @param earnedWeight  sum of test case weights that passed
     * @param totalWeight   sum of all test case weights
     * @param questionMarks the marks allocated to this question
     * @return final marks rounded to 2 decimal places
     */
    public static double computeFinalMarks(int earnedWeight, int totalWeight, int questionMarks) {
        if (totalWeight == 0 || questionMarks == 0) {
            return 0.0;
        }
        double ratio = (double) earnedWeight / (double) totalWeight;
        double raw = ratio * questionMarks;
        BigDecimal bd = BigDecimal.valueOf(raw).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // ========================================================================
    //  Result types
    // ========================================================================

    /**
     * Result item for a single test case execution.
     */
    public static class TestCaseResultItem {
        private int testCaseIndex;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private boolean passed;
        private long executionTimeMs;
        private long memoryKb;
        private boolean visibleToStudent = true;
        private String status; // PASS, FAIL, RUNTIME_ERROR, TIMEOUT

        public TestCaseResultItem() {}

        public int getTestCaseIndex() { return testCaseIndex; }
        public void setTestCaseIndex(int testCaseIndex) { this.testCaseIndex = testCaseIndex; }

        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }

        public String getExpectedOutput() { return expectedOutput; }
        public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

        public String getActualOutput() { return actualOutput; }
        public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }

        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }

        public long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

        public long getMemoryKb() { return memoryKb; }
        public void setMemoryKb(long memoryKb) { this.memoryKb = memoryKb; }

        public boolean isVisibleToStudent() { return visibleToStudent; }
        public void setVisibleToStudent(boolean visibleToStudent) { this.visibleToStudent = visibleToStudent; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * Aggregated evaluation result for a single question submission.
     */
    public static class EvaluationResult {
        private final boolean compiled;
        private final String compilationError;
        private final List<TestCaseResultItem> testCaseResults;
        private final String runtimeError;
        private final long totalExecutionTimeMs;
        private final long totalMemoryKb;
        private final double finalMarks;
        private final int earnedWeight;
        private final int totalWeight;

        public EvaluationResult(boolean compiled, String compilationError,
                                 List<TestCaseResultItem> testCaseResults, String runtimeError,
                                 long totalExecutionTimeMs, long totalMemoryKb,
                                 double finalMarks, int earnedWeight, int totalWeight) {
            this.compiled = compiled;
            this.compilationError = compilationError;
            this.testCaseResults = testCaseResults;
            this.runtimeError = runtimeError;
            this.totalExecutionTimeMs = totalExecutionTimeMs;
            this.totalMemoryKb = totalMemoryKb;
            this.finalMarks = finalMarks;
            this.earnedWeight = earnedWeight;
            this.totalWeight = totalWeight;
        }

        /**
         * Factory for compilation failure results.
         */
        public static EvaluationResult compileFailed(String compilationError, List<TestCase> testCases) {
            return compileFailed(compilationError, testCases, 0L);
        }

        /**
         * Factory for compilation failure results with compile time.
         */
        public static EvaluationResult compileFailed(String compilationError, List<TestCase> testCases, long compileTimeMs) {
            int totalWeight = testCases.stream().mapToInt(tc -> Math.max(0, tc.getWeight())).sum();
            List<TestCaseResultItem> emptyResults = new ArrayList<>();
            for (int i = 0; i < testCases.size(); i++) {
                TestCaseResultItem item = new TestCaseResultItem();
                item.setTestCaseIndex(i + 1);
                item.setInput(testCases.get(i).getInputData());
                item.setExpectedOutput(testCases.get(i).getExpectedOutput());
                item.setPassed(false);
                item.setActualOutput(null);
                item.setExecutionTimeMs(0);
                item.setMemoryKb(0);
                item.setVisibleToStudent(testCases.get(i).isSample());
                item.setStatus("SKIPPED");
                emptyResults.add(item);
            }
            return new EvaluationResult(false, compilationError, emptyResults, null,
                    compileTimeMs, 0L, 0.0, 0, totalWeight);
        }

        public boolean isCompiled() { return compiled; }
        public String getCompilationError() { return compilationError; }
        public List<TestCaseResultItem> getTestCaseResults() { return testCaseResults; }
        public String getRuntimeError() { return runtimeError; }
        public long getTotalExecutionTimeMs() { return totalExecutionTimeMs; }
        public long getTotalMemoryKb() { return totalMemoryKb; }
        public double getFinalMarks() { return finalMarks; }
        public int getEarnedWeight() { return earnedWeight; }
        public int getTotalWeight() { return totalWeight; }

        /**
         * Backward-compatible getter for ExamService.
         * @deprecated Use getFinalMarks() instead.
         */
        @Deprecated
        public int getTotalMarks() { return (int) Math.round(finalMarks); }

        /**
         * Backward-compatible getter for ExamService.
         * @deprecated Use getTotalWeight() instead.
         */
        @Deprecated
        public int getTotalPossibleMarks() { return totalWeight; }
    }
}

