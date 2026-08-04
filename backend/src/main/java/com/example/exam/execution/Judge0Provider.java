package com.example.exam.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link ExecutionProvider} that delegates to the Judge0 CE API.
 * <p>
 * If Judge0 is disabled or unreachable, callers should fall back
 * to {@link LocalCompilerProvider}.
 * </p>
 */
@Component
public class Judge0Provider implements ExecutionProvider {

    private static final Logger log = LoggerFactory.getLogger(Judge0Provider.class);

    private final Judge0ClientService judge0Client;

    public Judge0Provider(Judge0ClientService judge0Client) {
        this.judge0Client = judge0Client;
    }

    @Override
    public String providerName() {
        return "judge0";
    }

    /**
     * Returns true if Judge0 is configured and the client reports availability.
     */
    public boolean isAvailable() {
        return judge0Client.isAvailable();
    }

    @Override
    public ProviderResult compile(String language, String sourceCode) {
        if (!isAvailable()) {
            log.debug("Judge0 not available for compile, caller should fall back");
            return null;
        }
        try {
            ProviderResult result = judge0Client.compile(language, sourceCode);
            if (result == null) {
                log.warn("Judge0 compile returned null for lang={}, falling back", language);
            }
            return result;
        } catch (Exception e) {
            log.error("Judge0 compile error for lang={}: {}", language, e.getMessage());
            return null;
        }
    }

    @Override
    public ProviderResult execute(String language, String sourceCode, String stdin) {
        if (!isAvailable()) {
            log.debug("Judge0 not available for execute, caller should fall back");
            return null;
        }
        try {
            ProviderResult result = judge0Client.execute(language, sourceCode, stdin);
            if (result == null) {
                log.warn("Judge0 execute returned null for lang={}, falling back", language);
            }
            return result;
        } catch (Exception e) {
            log.error("Judge0 execute error for lang={}: {}", language, e.getMessage());
            return null;
        }
    }
}
