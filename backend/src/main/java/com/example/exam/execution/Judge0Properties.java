package com.example.exam.execution;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Judge0 code execution service.
 * <p>
 * Prefix: {@code judge0}
 * </p>
 * <pre>
 * judge0.enabled=true
 * judge0.url=https://judge0.example.com
 * judge0.api-key=your-api-key-here
 * judge0.connect-timeout-ms=5000
 * judge0.read-timeout-ms=30000
 * judge0.poll-interval-ms=500
 * judge0.max-poll-attempts=60
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "judge0")
public class Judge0Properties {

    /** Whether Judge0 integration is enabled. */
    private boolean enabled = false;

    /** Base URL of the Judge0 API (e.g. https://api.judge0.com). */
    private String url = "https://judge0-ce.p.rapidapi.com";

    /** API key for Judge0 (RapidAPI or self-hosted). */
    private String apiKey = "";

    /** Connection timeout in milliseconds. */
    private int connectTimeoutMs = 5000;

    /** Read/request timeout in milliseconds. */
    private int readTimeoutMs = 30000;

    /** Interval in ms between status polling attempts. */
    private int pollIntervalMs = 500;

    /** Maximum number of polling attempts before giving up. */
    private int maxPollAttempts = 60;

    // ── Getters and Setters ──

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    public int getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(int pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }

    public int getMaxPollAttempts() { return maxPollAttempts; }
    public void setMaxPollAttempts(int maxPollAttempts) { this.maxPollAttempts = maxPollAttempts; }
}

