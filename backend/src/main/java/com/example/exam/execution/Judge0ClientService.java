package com.example.exam.execution;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * HTTP client that communicates with the Judge0 CE API.
 * <p>
 * Supports submission creation (with token) and polling for results.
 * Handles language ID mapping and response parsing.
 * </p>
 */
@Service
public class Judge0ClientService {

    private static final Logger log = LoggerFactory.getLogger(Judge0ClientService.class);

    /**
     * Judge0 language IDs for supported languages.
     * @see <a href="https://ce.judge0.com/#languages">Judge0 Languages</a>
     */
    private static final java.util.Map<String, Integer> LANGUAGE_IDS = java.util.Map.of(
            "java", 62,       // Java (OpenJDK 13.0.1)
            "c", 50,          // C (GCC 9.2.0)
            "cpp", 54,        // C++ (GCC 9.2.0)
            "python", 71,     // Python (3.8.1)
            "javascript", 63  // JavaScript (Node.js 12.14.0)
    );

    /**
     * Judge0 status IDs that indicate completion.
     */
    private static final int STATUS_IN_QUEUE = 1;
    private static final int STATUS_PROCESSING = 2;
    private static final int STATUS_ACCEPTED = 3;
    private static final int STATUS_WRONG_ANSWER = 4;
    private static final int STATUS_TIME_LIMIT_EXCEEDED = 5;
    private static final int STATUS_COMPILATION_ERROR = 6;
    private static final int STATUS_RUNTIME_ERROR_SIGSEGV = 7;
    private static final int STATUS_RUNTIME_ERROR_SIGXFSZ = 8;
    private static final int STATUS_RUNTIME_ERROR_SIGFPE = 9;
    private static final int STATUS_RUNTIME_ERROR_SIGABRT = 10;
    private static final int STATUS_RUNTIME_ERROR_NZEC = 11;
    private static final int STATUS_RUNTIME_ERROR_OTHER = 12;
    private static final int STATUS_INTERNAL_ERROR = 13;
    private static final int STATUS_EXEC_FORMAT_ERROR = 14;

    private final Judge0Properties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public Judge0ClientService(Judge0Properties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
    }

    /**
     * Check if Judge0 is configured and enabled.
     */
    public boolean isAvailable() {
        return properties.isEnabled()
                && properties.getUrl() != null && !properties.getUrl().isBlank();
    }

    /**
     * Submit source code to Judge0 for compilation only.
     *
     * @param language   normalized language name
     * @param sourceCode the source code
     * @return ProviderResult with compile status
     */
    public ProviderResult compile(String language, String sourceCode) {
        return submitAndPoll(language, sourceCode, "", true);
    }

    /**
     * Submit source code to Judge0 for full execution (compile + run).
     *
     * @param language   normalized language name
     * @param sourceCode the source code
     * @param stdin      standard input for the program
     * @return ProviderResult with execution status and output
     */
    public ProviderResult execute(String language, String sourceCode, String stdin) {
        return submitAndPoll(language, sourceCode, stdin != null ? stdin : "", false);
    }

    // ── Internal: submit and poll ──

    private ProviderResult submitAndPoll(String language, String sourceCode, String stdin, boolean compileOnly) {
        Integer langId = LANGUAGE_IDS.get(language);
        if (langId == null) {
            return ProviderResult.compileFailed("Unsupported language for Judge0: " + language);
        }

        try {
            // 1. Create submission
            String token = createSubmission(sourceCode, langId, stdin, compileOnly);
            if (token == null) {
                log.warn("Judge0 submission creation failed for lang={}, falling back", language);
                return null; // signal fallback
            }

            // 2. Poll for result
            JsonNode result = pollForResult(token);
            if (result == null) {
                log.warn("Judge0 polling failed for token={}, falling back", token);
                return null;
            }

            // 3. Convert to ProviderResult
            return parseResult(result, compileOnly);

        } catch (Exception e) {
            log.error("Judge0 execution error for lang={}: {}", language, e.getMessage());
            return null; // signal fallback
        }
    }

    /**
     * Create a submission in Judge0 and return the token.
     */
    private String createSubmission(String sourceCode, int languageId, String stdin, boolean compileOnly)
            throws Exception {

        ObjectNode body = objectMapper.createObjectNode();
        body.put("source_code", sourceCode);
        body.put("language_id", languageId);
        body.put("stdin", stdin);
        body.put("redirect_stderr_to_stdout", false);

        // Set time/memory limits from config
        body.put("cpu_time_limit", 5.0);
        body.put("memory_limit", 256000); // KB
        body.put("max_file_size", 1024);

        // Compile-only: don't run the compiled program
        if (compileOnly) {
            body.putNull("expected_output");
        }

        // Build URL: POST /submissions?base64_encoded=false&wait=false
        String urlStr = properties.getUrl();
        if (!urlStr.endsWith("/")) urlStr += "/";
        String submitUrl = urlStr + "submissions?base64_encoded=false&wait=false";

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(submitUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(body)));

        // Add API key header if present
        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            builder.header("X-RapidAPI-Key", properties.getApiKey());
        }

        HttpResponse<String> response = httpClient.send(builder.build(), BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            log.warn("Judge0 submission failed: HTTP {} body={}", response.statusCode(), response.body());
            return null;
        }

        JsonNode json = objectMapper.readTree(response.body());
        String token = json.has("token") ? json.get("token").asText() : null;
        if (token == null || token.isBlank()) {
            log.warn("Judge0 returned no token: {}", response.body());
            return null;
        }

        log.debug("Judge0 submission created: token={}", token);
        return token;
    }

    /**
     * Poll Judge0 for the result of a submission.
     */
    private JsonNode pollForResult(String token) throws Exception {
        String urlStr = properties.getUrl();
        if (!urlStr.endsWith("/")) urlStr += "/";
        String getUrl = urlStr + "submissions/" + token + "?base64_encoded=false";

        int maxAttempts = properties.getMaxPollAttempts();
        int pollIntervalMs = properties.getPollIntervalMs();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Thread.sleep(pollIntervalMs);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getUrl))
                    .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                    .GET();

            if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
                builder.header("X-RapidAPI-Key", properties.getApiKey());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Judge0 poll failed: HTTP {} body={}", response.statusCode(), response.body());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            int statusId = json.has("status") && json.get("status").has("id")
                    ? json.get("status").get("id").asInt()
                    : STATUS_PROCESSING;

            // If status indicates completion, parse and return
            if (statusId != STATUS_IN_QUEUE && statusId != STATUS_PROCESSING) {
                return json;
            }
        }

        log.warn("Judge0 poll timed out for token={} after {} attempts", token, maxAttempts);
        return null;
    }

    /**
     * Convert Judge0 JSON result to a ProviderResult.
     */
    private ProviderResult parseResult(JsonNode result, boolean compileOnly) {
        int statusId = result.has("status") && result.get("status").has("id")
                ? result.get("status").get("id").asInt()
                : STATUS_INTERNAL_ERROR;

        String stdout = result.has("stdout") && !result.get("stdout").isNull()
                ? result.get("stdout").asText() : "";
        String stderr = result.has("stderr") && !result.get("stderr").isNull()
                ? result.get("stderr").asText() : "";
        String compileOutput = result.has("compile_output") && !result.get("compile_output").isNull()
                ? result.get("compile_output").asText() : "";

        long timeMs = result.has("time") && !result.get("time").isNull()
                ? (long) (result.get("time").asDouble() * 1000) : 0L;
        long memoryKb = result.has("memory") && !result.get("memory").isNull()
                ? result.get("memory").asLong() : 0L;

        if (compileOnly && statusId == STATUS_ACCEPTED) {
            return ProviderResult.compilationSucceeded(timeMs);
        }

        switch (statusId) {
            case STATUS_ACCEPTED:
                return ProviderResult.executionSucceeded(stdout, timeMs, memoryKb);

            case STATUS_COMPILATION_ERROR:
                String ceMsg = compileOutput != null && !compileOutput.isBlank()
                        ? compileOutput : "Compilation error";
                return ProviderResult.compileFailed(ceMsg, timeMs);

            case STATUS_TIME_LIMIT_EXCEEDED:
                return ProviderResult.timedOut(timeMs);

            case STATUS_WRONG_ANSWER:
                // Wrong answer is treated as successful execution with incorrect output
                return ProviderResult.executionSucceeded(stdout, timeMs, memoryKb);

            default:
                // Runtime errors
                String rtError = stderr != null && !stderr.isBlank()
                        ? stderr : "Runtime error (status: " + statusId + ")";
                if (compileOnly) {
                    return ProviderResult.compileFailed(compileOutput != null && !compileOutput.isBlank()
                            ? compileOutput : rtError, timeMs);
                }
                return ProviderResult.runtimeFailed(rtError, stdout, timeMs);
        }
    }
}

