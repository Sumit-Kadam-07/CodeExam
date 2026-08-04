package com.example.exam.ai;

import org.springframework.stereotype.Component;

import com.example.exam.dto.AiGenerateRequest;

@Component
public class PromptBuilder {

    public String buildQuestionPrompt(AiGenerateRequest request) {
        String lang = request.getLanguage();
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert programming question writer for coding exams.\n\n");
        sb.append("Generate a coding question with the following specifications:\n");
        sb.append("- Topic: ").append(request.getTopic()).append("\n");
        sb.append("- Programming Language: ").append(lang).append("\n");
        sb.append("- Difficulty: ").append(request.getDifficulty()).append("\n");
        sb.append("- Marks: ").append(request.getMarks()).append("\n\n");
        sb.append("Return ONLY a valid JSON object with this exact structure (no markdown, no code fences):\n");
        sb.append("{\n");
        sb.append("  \"title\": \"Question title\",\n");
        sb.append("  \"problemStatement\": \"Detailed problem description (2-4 paragraphs)\",\n");
        sb.append("  \"inputFormat\": \"Description of input format\",\n");
        sb.append("  \"outputFormat\": \"Description of output format\",\n");
        sb.append("  \"constraints\": \"List of constraints\",\n");
        sb.append("  \"sampleInput\": \"Sample input example\",\n");
        sb.append("  \"sampleOutput\": \"Sample output example\",\n");
        sb.append("  \"explanation\": \"Explanation of the expected solution\",\n");
        sb.append("  \"starterCode\": \"Starter/boilerplate code in ").append(lang).append("\",\n");
        sb.append("  \"expectedSolution\": \"Complete working solution in ").append(lang).append("\",\n");
        sb.append("  \"testCases\": [\n");
        sb.append("    {\"input\": \"test input 1\", \"expectedOutput\": \"expected output 1\", \"weight\": 1, \"sample\": true},\n");
        sb.append("    {\"input\": \"test input 2\", \"expectedOutput\": \"expected output 2\", \"weight\": 2, \"sample\": false},\n");
        sb.append("    {\"input\": \"test input 3\", \"expectedOutput\": \"expected output 3\", \"weight\": 2, \"sample\": false},\n");
        sb.append("    {\"input\": \"test input 4\", \"expectedOutput\": \"expected output 4\", \"weight\": 3, \"sample\": false},\n");
        sb.append("    {\"input\": \"test input 5\", \"expectedOutput\": \"expected output 5\", \"weight\": 2, \"sample\": false}\n");
        sb.append("  ]\n");
        sb.append("}\n\n");
        sb.append("Generate exactly 5 test cases. Make the first one a sample test case. ");
        sb.append("Ensure all test cases are correct and match the problem statement. ");
        sb.append("Return ONLY the JSON object, nothing else.");
        return sb.toString();
    }

public String buildQuestionPromptFromText(String extractedText, String language, String difficulty, int marks) {
        AiGenerateRequest request = new AiGenerateRequest();
        request.setTopic(extractedText);
        request.setLanguage(language);
        request.setDifficulty(difficulty);
        request.setMarks(marks);
        return buildQuestionPrompt(request);
    }

    /**
     * Build a prompt that instructs the AI to parse multiple coding questions from
     * uploaded file content and return them as a JSON array.
     * Each question is treated independently with full metadata.
     */
    public String buildMultiQuestionPrompt(String extractedText, String language, String difficulty, int marks) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert programming question parser for coding exams.\n\n");
        sb.append("Below is the content extracted from a document. It may contain one or more coding questions.\n\n");
        sb.append("--- DOCUMENT CONTENT ---\n");
        sb.append(extractedText).append("\n");
        sb.append("--- END OF DOCUMENT ---\n\n");
        sb.append("=== INSTRUCTIONS ===\n");
        sb.append("1. Detect EVERY coding question in the document. Treat each question independently.\n");
        sb.append("2. NEVER merge multiple questions into one. Each question must be a separate entry.\n");
        sb.append("3. Skip blank pages, irrelevant content, and duplicate questions.\n");
        sb.append("4. If you cannot detect any valid coding question, return an empty array [].\n");
        sb.append("5. For each detected question, generate complete metadata including:\n");
        sb.append("   - title: A concise title for the question\n");
        sb.append("   - problemStatement: Full problem description (2-4 paragraphs)\n");
        sb.append("   - inputFormat: Description of input format (or empty string if not specified)\n");
        sb.append("   - outputFormat: Description of output format (or empty string if not specified)\n");
        sb.append("   - constraints: Constraints and limitations (or empty string if not specified)\n");
        sb.append("   - sampleInput: Example input (or empty string)\n");
        sb.append("   - sampleOutput: Example output (or empty string)\n");
        sb.append("   - explanation: Explanation of the expected solution approach\n");
        sb.append("   - starterCode: Starter/boilerplate code in ").append(language).append(" (or empty string)\n");
        sb.append("   - expectedSolution: Complete working solution in ").append(language).append(" (or empty string)\n");
        sb.append("   - difficulty: \"").append(difficulty).append("\"\n");
        sb.append("   - marks: ").append(marks).append("\n");
        sb.append("   - testCases: Array of test case objects with fields: input, expectedOutput, weight (int), sample (boolean). Generate at least 3-5 test cases per question.\n\n");
        sb.append("6. Return ONLY a valid JSON array (no markdown, no code fences, no extra text).\n\n");
        sb.append("Example response format:\n");
        sb.append("[\n");
        sb.append("  {\n");
        sb.append("    \"title\": \"Question 1 Title\",\n");
        sb.append("    \"problemStatement\": \"Description...\",\n");
        sb.append("    \"inputFormat\": \"Input format description\",\n");
        sb.append("    \"outputFormat\": \"Output format description\",\n");
        sb.append("    \"constraints\": \"Constraints\",\n");
        sb.append("    \"sampleInput\": \"Sample input\",\n");
        sb.append("    \"sampleOutput\": \"Sample output\",\n");
        sb.append("    \"explanation\": \"Solution explanation\",\n");
        sb.append("    \"starterCode\": \"public class Main { ... }\",\n");
        sb.append("    \"expectedSolution\": \"public class Main { ... }\",\n");
        sb.append("    \"difficulty\": \"Medium\",\n");
        sb.append("    \"marks\": 10,\n");
        sb.append("    \"testCases\": [\n");
        sb.append("      {\"input\": \"5\", \"expectedOutput\": \"25\", \"weight\": 1, \"sample\": true},\n");
        sb.append("      {\"input\": \"10\", \"expectedOutput\": \"100\", \"weight\": 2, \"sample\": false}\n");
        sb.append("    ]\n");
        sb.append("  }\n");
        sb.append("]\n\n");
        sb.append("Return ONLY the JSON array. Nothing else.");
        return sb.toString();
    }
}
