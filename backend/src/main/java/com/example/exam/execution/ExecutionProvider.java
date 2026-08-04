package com.example.exam.execution;

/**
 * Abstraction for code execution engines.
 * <p>
 * Implementations can be local compiler-based execution or remote
 * providers such as Judge0.
 * </p>
 */
public interface ExecutionProvider {

    /**
     * Unique identifier for this provider (e.g. "local", "judge0").
     */
    String providerName();

    /**
     * Compile source code without executing it.
     *
     * @param language   normalized language name (java, c, cpp, python, javascript)
     * @param sourceCode the source code to compile
     * @return compilation result
     */
    ProviderResult compile(String language, String sourceCode);

    /**
     * Execute (compile + run) source code with optional stdin.
     *
     * @param language   normalized language name
     * @param sourceCode the source code to execute
     * @param stdin      standard input to pass to the program (may be empty)
     * @return execution result
     */
    ProviderResult execute(String language, String sourceCode, String stdin);
}
