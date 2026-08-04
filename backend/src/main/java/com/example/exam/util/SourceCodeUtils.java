package com.example.exam.util;

import java.util.List;

import com.example.exam.model.TestCase;

/**
 * Shared source-code utilities for language normalization, class name extraction,
 * output comparison, and marks calculation.
 * 
 * <p>Extracted from {@code CodingEvaluationService} and {@code CodeExecutionService}
 * to eliminate DRY violations. Behaviour is identical to the original duplicated
 * implementations.</p>
 */
public final class SourceCodeUtils {

    private SourceCodeUtils() {
        // utility class
    }

    // ─────────────────────────────────────────────────
    //  Language normalization
    // ─────────────────────────────────────────────────

    /**
     * Normalize a language string to a canonical lowercase form.
     * 
     * <p>Supports: Java, C, C++, Python, JavaScript (and lower-case equivalents).</p>
     *
     * @param language raw language string (may be null)
     * @return normalized lower-case language name, or empty string for null
     */
    public static String normalize(String language) {
        if (language == null) return "";
        return switch (language.trim()) {
            case "Java" -> "java";
            case "C" -> "c";
            case "C++" -> "cpp";
            case "Python" -> "python";
            case "JavaScript" -> "javascript";
            default -> language.trim().toLowerCase();
        };
    }

    // ─────────────────────────────────────────────────
    //  Class name extraction
    // ─────────────────────────────────────────────────

    /**
     * Extract the primary class name from Java source code.
     * 
     * <p>Matches {@code public class ClassName} first, then {@code class ClassName}.</p>
     *
     * @param src Java source code (may be null)
     * @return the class name, or {@code null} if not found
     */
    public static String extractPrimaryClassName(String src) {
        if (src == null) return null;
        int idx = src.indexOf("public class ");
        if (idx >= 0) {
            return readIdentifier(src, idx + "public class ".length());
        }
        idx = src.indexOf("class ");
        if (idx >= 0) {
            return readIdentifier(src, idx + "class ".length());
        }
        return null;
    }

    /**
     * Read a Java identifier starting at the given position.
     *
     * @param s     source string
     * @param start start position to scan from
     * @return the identifier, or {@code null} if none found
     */
    public static String readIdentifier(String s, int start) {
        int i = start;
        while (i < s.length() && !Character.isJavaIdentifierStart(s.charAt(i))) {
            i++;
        }
        if (i >= s.length()) return null;
        int j = i + 1;
        while (j < s.length() && Character.isJavaIdentifierPart(s.charAt(j))) {
            j++;
        }
        return s.substring(i, j);
    }

    // ─────────────────────────────────────────────────
    //  Output comparison
    // ─────────────────────────────────────────────────

    /**
     * Compare expected vs actual output.
     * 
     * <p>Rules: ignore trailing spaces, ignore extra blank lines,
     * trim whitespace, use exact logical comparison.</p>
     *
     * @param expected expected output string
     * @param actual   actual output string from the student's code
     * @return true if the outputs match after normalization
     */
    public static boolean compareOutput(String expected, String actual) {
        String exp = normalizeOutput(expected);
        String act = normalizeOutput(actual);
        return exp.equals(act);
    }

    /**
     * Normalize output for comparison: trim whitespace, normalize line endings,
     * remove trailing spaces on each line, remove trailing blank lines.
     *
     * @param s raw output string (may be null)
     * @return normalized output string
     */
    public static String normalizeOutput(String s) {
        if (s == null) return "";
        // Normalize line endings
        s = s.replace("\r\n", "\n").replace("\r", "\n");
        // Trim trailing whitespace on each line
        String[] lines = s.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].stripTrailing();
            if (i > 0) sb.append("\n");
            sb.append(trimmed);
        }
        // Remove trailing blank lines
        String result = sb.toString();
        while (result.endsWith("\n\n")) {
            result = result.substring(0, result.length() - 1);
        }
        return result.stripTrailing();
    }

    // ─────────────────────────────────────────────────
    //  Marks calculation
    // ─────────────────────────────────────────────────

    /**
     * Calculate the total possible marks across all test cases.
     *
     * @param testCases list of test cases (may be null)
     * @return sum of all test case weights (minimum 0)
     */
    public static int totalPossibleMarks(List<TestCase> testCases) {
        if (testCases == null) return 0;
        return testCases.stream().mapToInt(tc -> Math.max(0, tc.getWeight())).sum();
    }
}
