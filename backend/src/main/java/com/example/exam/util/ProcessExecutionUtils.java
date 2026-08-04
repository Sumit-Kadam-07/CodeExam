package com.example.exam.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Shared process execution utilities for compiling and running code.
 * 
 * <p>Extracted from {@code CodingEvaluationService} and {@code CodeExecutionService}
 * to eliminate DRY violations. Behaviour is identical to the original duplicated
 * implementations.</p>
 */
public final class ProcessExecutionUtils {

    /** Maximum number of characters to retain from process output. */
    public static final long MAX_OUTPUT_CHARS = 20000;

    private ProcessExecutionUtils() {
        // utility class
    }

    // ─────────────────────────────────────────────────
    //  ExecResult – shared process result type
    // ─────────────────────────────────────────────────

    /**
     * Holds the result of executing a process (compile or run).
     * 
     * <p>Fields are package-visible for direct access, matching the original
     * inner-class conventions in both caller services.</p>
     */
    public static class ExecResult {
        /** Process exit code (0 normally indicates success). */
        public int exitCode;
        /** Standard output (raw, not trimmed). */
        public String stdout;
        /** Standard error (raw, not trimmed). */
        public String stderr;
        /** Combined stdout + stderr (handy for compile errors). */
        public String combinedOutput;
        /** Wall-clock execution time in milliseconds. */
        public long executionTimeMs;
    }

    // ─────────────────────────────────────────────────
    //  Process execution
    // ─────────────────────────────────────────────────

    /**
     * Execute a process with a timeout.
     * 
     * <p>Stores stdout / stderr {@link #readAll readAll} without trimming.
     * Callers that need truncation apply {@link #trimTo(String, long)}
     * after the call — matching the original CodeExecutionService pattern.</p>
     *
     * @param pb        configured ProcessBuilder
     * @param stdinBytes bytes to write to stdin (may be null / empty)
     * @param timeoutMs per-call timeout in milliseconds
     * @return populated ExecResult (never null)
     */
    public static ExecResult executeWithTimeout(ProcessBuilder pb,
                                                 byte[] stdinBytes,
                                                 long timeoutMs) {
        long start = System.nanoTime();
        try {
            pb.redirectErrorStream(false);
            Process process = pb.start();

            if (stdinBytes != null && stdinBytes.length > 0) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(stdinBytes);
                    os.flush();
                }
            } else {
                process.getOutputStream().close();
            }

            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            long elapsedMs = toMs(System.nanoTime() - start);

            if (!finished) {
                process.destroyForcibly();
                ExecResult r = new ExecResult();
                r.exitCode = 124;
                r.stdout = "";
                r.stderr = "Time Limit Exceeded after " + timeoutMs + "ms";
                r.combinedOutput = r.stderr;
                r.executionTimeMs = elapsedMs;
                return r;
            }

            String out = readAll(process.getInputStream());
            String err = readAll(process.getErrorStream());
            int exit = process.exitValue();

            ExecResult r = new ExecResult();
            r.exitCode = exit;
            r.stdout = out;
            r.stderr = err;
            r.executionTimeMs = elapsedMs;
            r.combinedOutput = (out == null ? "" : out)
                    + (err == null || err.isBlank() ? "" : "\n" + err);
            return r;
        } catch (Exception e) {
            ExecResult r = new ExecResult();
            r.exitCode = 1;
            r.stdout = "";
            r.stderr = e.getMessage();
            r.combinedOutput = e.getMessage();
            r.executionTimeMs = toMs(System.nanoTime() - start);
            return r;
        }
    }

    // ─────────────────────────────────────────────────
    //  I/O helpers
    // ─────────────────────────────────────────────────

    /**
     * Read an entire input stream into a String (UTF-8).
     * Lines are separated by {@code '\n'}.
     * ANSI escape sequences are automatically stripped from the output
     * to ensure clean compiler error messages.
     */
    public static String readAll(java.io.InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(stripAnsi(line)).append("\n");
            }
        }
        return sb.toString();
    }

/**
     * Strip ANSI escape sequences from a string.
     * Handles common ANSI codes used by compilers (gcc, g++, javac) for colored output.
     *
     * @param s input string that may contain ANSI codes (may be null)
     * @return clean string without ANSI escape sequences, or null if input was null
     */
    public static String stripAnsi(String s) {
        if (s == null) return null;
        // Match ANSI escape sequences: ESC [ <parameters> <command>
        // For example: \u001B[01;31m (red text), \u001B[0m (reset)
        // Must use proper Java string escaping with double backslashes for regex
        String result = s.replaceAll("\\u001B\\[[;\\d]*[ -/]*[@-~]", "");
        // Match OSC sequences: ESC ] <number> ; <text> BEL
        result = result.replaceAll("\\u001B\\][0-9;]*(\u0007|\\u001B\\)?)", "");
        // Match remaining control characters (except tab, newline, carriage return)
        result = result.replaceAll("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F]", "");
        return result;
    }

    /**
     * Truncate a string to at most {@code maxChars} characters.
     *
     * @param s        input string (may be null)
     * @param maxChars maximum length before truncation
     * @return original string, truncated version, or null
     */
    public static String trimTo(String s, long maxChars) {
        if (s == null) return null;
        if (s.length() <= maxChars) return s;
        return s.substring(0, (int) maxChars) + "\n... (truncated)";
    }

    /**
     * Convert nanoseconds to milliseconds (minimum 0).
     */
    public static long toMs(long nanos) {
        return Math.max(0, TimeUnit.NANOSECONDS.toMillis(nanos));
    }

    // ─────────────────────────────────────────────────
    //  Temp directory management
    // ─────────────────────────────────────────────────

    /**
     * Create a temporary directory with the given prefix.
     *
     * @param prefix directory name prefix
     * @return Path to the new directory, or {@code null} on failure
     */
    public static Path createTempDir(String prefix) {
        try {
            return Files.createTempDirectory(prefix + UUID.randomUUID());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Recursively delete a file or directory.
     * Silently ignores failures (best-effort cleanup).
     */
    public static void deleteRecursively(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) {
                    deleteRecursively(c);
                }
            }
        }
        try {
            f.delete();
        } catch (Exception ignore) {
            // best-effort
        }
    }
}

