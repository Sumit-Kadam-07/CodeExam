package com.example.exam.execution;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.exam.util.ProcessExecutionUtils;
import com.example.exam.util.ProcessExecutionUtils.ExecResult;
import com.example.exam.util.SourceCodeUtils;

/**
 * {@link ExecutionProvider} that executes code using locally installed
 * compilers and interpreters (javac, gcc, g++, python, node).
 * <p>
 * This is the default fallback provider used when Judge0 is unavailable
 * or disabled.
 * </p>
 */
@Component
public class LocalCompilerProvider implements ExecutionProvider {

    private static final Logger log = LoggerFactory.getLogger(LocalCompilerProvider.class);
    private static final long TIMEOUT_MS = 5000;
    private static final long MAX_OUTPUT_CHARS = ProcessExecutionUtils.MAX_OUTPUT_CHARS;

    @Override
    public String providerName() {
        return "local";
    }

    @Override
    public ProviderResult compile(String language, String sourceCode) {
        String lang = SourceCodeUtils.normalize(language);
        if (sourceCode == null || sourceCode.isBlank()) {
            return ProviderResult.compileFailed("Empty source code.");
        }

        Path workDir = ProcessExecutionUtils.createTempDir("code-exec-");
        if (workDir == null) {
            return ProviderResult.compileFailed("Unable to create temp directory.");
        }
        try {
            return compileInDir(workDir, lang, sourceCode);
        } catch (IOException e) {
            log.error("Local compile IO error for lang={}: {}", lang, e.getMessage());
            return ProviderResult.compileFailed("IO error: " + e.getMessage());
        } finally {
            ProcessExecutionUtils.deleteRecursively(workDir.toFile());
        }
    }

    @Override
    public ProviderResult execute(String language, String sourceCode, String stdin) {
        String lang = SourceCodeUtils.normalize(language);
        if (sourceCode == null || sourceCode.isBlank()) {
            return ProviderResult.runtimeFailed("Empty source code.", null, 0);
        }

        Path workDir = ProcessExecutionUtils.createTempDir("code-exec-");
        if (workDir == null) {
            return ProviderResult.runtimeFailed("Unable to create temp directory.", null, 0);
        }
        try {
            return executeInDir(workDir, lang, sourceCode, stdin == null ? "" : stdin);
        } catch (IOException e) {
            log.error("Local execute IO error for lang={}: {}", lang, e.getMessage());
            return ProviderResult.runtimeFailed("IO error: " + e.getMessage(), null, 0);
        } finally {
            ProcessExecutionUtils.deleteRecursively(workDir.toFile());
        }
    }

    // ── Compile ──

    private ProviderResult compileInDir(Path workDir, String lang, String sourceCode) throws IOException {
        return switch (lang) {
            case "java" -> compileJava(workDir, sourceCode);
            case "c" -> compileC(workDir, sourceCode);
            case "cpp" -> compileCpp(workDir, sourceCode);
            default -> ProviderResult.compilationSucceeded(); // scripting languages
        };
    }

    // ── Execute ──

    private ProviderResult executeInDir(Path workDir, String lang, String sourceCode, String input) throws IOException {
        return switch (lang) {
            case "java" -> runJava(workDir, sourceCode, input, TIMEOUT_MS);
            case "c" -> runC(workDir, sourceCode, input, TIMEOUT_MS);
            case "cpp" -> runCpp(workDir, sourceCode, input, TIMEOUT_MS);
            case "python" -> runPython(workDir, sourceCode, input, TIMEOUT_MS);
            case "javascript" -> runJavaScript(workDir, sourceCode, input, TIMEOUT_MS);
            default -> ProviderResult.runtimeFailed("Unsupported language: " + lang, null, 0);
        };
    }

    // ── Java ──

    private ProviderResult compileJava(Path workDir, String sourceCode) throws IOException {
        Path srcDir = workDir.resolve("src");
        Path outDir = workDir.resolve("out");
        Files.createDirectories(srcDir);
        Files.createDirectories(outDir);

        String className = SourceCodeUtils.extractPrimaryClassName(sourceCode);
        if (className == null) className = "Solution";

        Path javaFile = srcDir.resolve(className + ".java");
        Files.writeString(javaFile, sourceCode, StandardCharsets.UTF_8);

        long compileStart = System.nanoTime();
        ProcessBuilder pb = new ProcessBuilder("javac", "-encoding", "UTF-8",
                "-d", outDir.toAbsolutePath().toString(),
                javaFile.toAbsolutePath().toString());
        pb.redirectErrorStream(true);

        ExecResult r = ProcessExecutionUtils.executeWithTimeout(pb, null, TIMEOUT_MS);
        long ms = ProcessExecutionUtils.toMs(System.nanoTime() - compileStart);

        if (r.exitCode != 0) {
            return ProviderResult.compileFailed(ProcessExecutionUtils.trimTo(r.combinedOutput, MAX_OUTPUT_CHARS), ms);
        }
        return ProviderResult.compilationSucceeded(ms);
    }

    private ProviderResult runJava(Path workDir, String sourceCode, String input, long timeoutMs) throws IOException {
        Path srcDir = workDir.resolve("src");
        Path outDir = workDir.resolve("out");
        Files.createDirectories(srcDir);
        Files.createDirectories(outDir);

        String className = SourceCodeUtils.extractPrimaryClassName(sourceCode);
        if (className == null) className = "Solution";

        Path javaFile = srcDir.resolve(className + ".java");
        Files.writeString(javaFile, sourceCode, StandardCharsets.UTF_8);

        // Compile
        ProcessBuilder javacPb = new ProcessBuilder("javac", "-encoding", "UTF-8",
                "-d", outDir.toAbsolutePath().toString(),
                javaFile.toAbsolutePath().toString());
        javacPb.redirectErrorStream(true);

        ExecResult compile = ProcessExecutionUtils.executeWithTimeout(javacPb, null, Math.min(2000, timeoutMs));
        if (compile.exitCode != 0) {
            return ProviderResult.compileFailed(ProcessExecutionUtils.trimTo(compile.combinedOutput, MAX_OUTPUT_CHARS));
        }

        // Run
        long runStart = System.nanoTime();
        ProcessBuilder runPb = new ProcessBuilder("java", "-Xmx256m",
                "-cp", outDir.toAbsolutePath().toString(), className);
        runPb.directory(workDir.toFile());

        ExecResult run = ProcessExecutionUtils.executeWithTimeout(runPb, input.getBytes(StandardCharsets.UTF_8), timeoutMs);
        long totalMs = ProcessExecutionUtils.toMs(System.nanoTime() - runStart);

        return fromExecResult(run, totalMs);
    }

    // ── C ──

    private ProviderResult compileC(Path workDir, String sourceCode) throws IOException {
        Path srcFile = workDir.resolve("Main.c");
        Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);
        Path outFile = workDir.resolve("a.out");

        ProcessBuilder pb = new ProcessBuilder("gcc", srcFile.toAbsolutePath().toString(),
                "-O2", "-pipe", "-o", outFile.toAbsolutePath().toString());
        pb.redirectErrorStream(true);

        ExecResult r = ProcessExecutionUtils.executeWithTimeout(pb, null, TIMEOUT_MS);
        if (r.exitCode != 0) {
            return ProviderResult.compileFailed(ProcessExecutionUtils.trimTo(r.combinedOutput, MAX_OUTPUT_CHARS));
        }
        return ProviderResult.compilationSucceeded();
    }

    private ProviderResult runC(Path workDir, String sourceCode, String input, long timeoutMs) throws IOException {
        Path srcFile = workDir.resolve("Main.c");
        Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);
        Path outFile = workDir.resolve("a.out");

        ProcessBuilder gccPb = new ProcessBuilder("gcc", srcFile.toAbsolutePath().toString(),
                "-O2", "-pipe", "-o", outFile.toAbsolutePath().toString());
        gccPb.redirectErrorStream(true);
        ExecResult compile = ProcessExecutionUtils.executeWithTimeout(gccPb, null, Math.min(2000, timeoutMs));
        if (compile.exitCode != 0) {
            return ProviderResult.compileFailed(ProcessExecutionUtils.trimTo(compile.combinedOutput, MAX_OUTPUT_CHARS));
        }

        long runStart = System.nanoTime();
        ProcessBuilder runPb = new ProcessBuilder(outFile.toAbsolutePath().toString());
        ExecResult run = ProcessExecutionUtils.executeWithTimeout(runPb, input.getBytes(StandardCharsets.UTF_8), timeoutMs);
        long totalMs = ProcessExecutionUtils.toMs(System.nanoTime() - runStart);

        return fromExecResult(run, totalMs);
    }

    // ── C++ ──

    private ProviderResult compileCpp(Path workDir, String sourceCode) throws IOException {
        Path srcFile = workDir.resolve("Main.cpp");
        Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);
        Path outFile = workDir.resolve("a.out");

        ProcessBuilder pb = new ProcessBuilder("g++", srcFile.toAbsolutePath().toString(),
                "-O2", "-pipe", "-o", outFile.toAbsolutePath().toString());
        pb.redirectErrorStream(true);

        ExecResult r = ProcessExecutionUtils.executeWithTimeout(pb, null, TIMEOUT_MS);
        if (r.exitCode != 0) {
            return ProviderResult.compileFailed(ProcessExecutionUtils.trimTo(r.combinedOutput, MAX_OUTPUT_CHARS));
        }
        return ProviderResult.compilationSucceeded();
    }

    private ProviderResult runCpp(Path workDir, String sourceCode, String input, long timeoutMs) throws IOException {
        Path srcFile = workDir.resolve("Main.cpp");
        Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);
        Path outFile = workDir.resolve("a.out");

        ProcessBuilder gppPb = new ProcessBuilder("g++", srcFile.toAbsolutePath().toString(),
                "-O2", "-pipe", "-o", outFile.toAbsolutePath().toString());
        gppPb.redirectErrorStream(true);
        ExecResult compile = ProcessExecutionUtils.executeWithTimeout(gppPb, null, Math.min(2000, timeoutMs));
        if (compile.exitCode != 0) {
            return ProviderResult.compileFailed(ProcessExecutionUtils.trimTo(compile.combinedOutput, MAX_OUTPUT_CHARS));
        }

        long runStart = System.nanoTime();
        ProcessBuilder runPb = new ProcessBuilder(outFile.toAbsolutePath().toString());
        ExecResult run = ProcessExecutionUtils.executeWithTimeout(runPb, input.getBytes(StandardCharsets.UTF_8), timeoutMs);
        long totalMs = ProcessExecutionUtils.toMs(System.nanoTime() - runStart);

        return fromExecResult(run, totalMs);
    }

    // ── Python ──

    private ProviderResult runPython(Path workDir, String sourceCode, String input, long timeoutMs) throws IOException {
        Path srcFile = workDir.resolve("main.py");
        Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);

        long runStart = System.nanoTime();
        ProcessBuilder runPb = new ProcessBuilder("python", srcFile.toAbsolutePath().toString());
        ExecResult run = ProcessExecutionUtils.executeWithTimeout(runPb, input.getBytes(StandardCharsets.UTF_8), timeoutMs);
        long totalMs = ProcessExecutionUtils.toMs(System.nanoTime() - runStart);

        return fromExecResult(run, totalMs);
    }

    // ── JavaScript ──

    private ProviderResult runJavaScript(Path workDir, String sourceCode, String input, long timeoutMs) throws IOException {
        Path srcFile = workDir.resolve("main.js");
        Files.writeString(srcFile, sourceCode, StandardCharsets.UTF_8);

        long runStart = System.nanoTime();
        ProcessBuilder runPb = new ProcessBuilder("node", srcFile.toAbsolutePath().toString());
        ExecResult run = ProcessExecutionUtils.executeWithTimeout(runPb, input.getBytes(StandardCharsets.UTF_8), timeoutMs);
        long totalMs = ProcessExecutionUtils.toMs(System.nanoTime() - runStart);

        return fromExecResult(run, totalMs);
    }

    // ── Shared helper ──

    private ProviderResult fromExecResult(ExecResult run, long executionTimeMs) {
        String out = ProcessExecutionUtils.trimTo(run.stdout, MAX_OUTPUT_CHARS);
        String err = ProcessExecutionUtils.trimTo(run.stderr, MAX_OUTPUT_CHARS);

        if (run.exitCode == 0) {
            return ProviderResult.executionSucceeded(out, executionTimeMs, null);
        }
        return ProviderResult.runtimeFailed(
                err != null && !err.isBlank() ? err : "Runtime error (exit code: " + run.exitCode + ")",
                out, executionTimeMs);
    }
}

