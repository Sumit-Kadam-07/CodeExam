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

    sb.append("Below is the content extracted from a document. ");
    sb.append("It may contain one or more coding questions.\n\n");

    sb.append("--- DOCUMENT CONTENT ---\n");
    sb.append(extractedText);
    sb.append("\n--- END OF DOCUMENT ---\n\n");

    sb.append("=== INSTRUCTIONS ===\n");

    sb.append("1. Detect EVERY coding question in the document.\n");
    sb.append("2. NEVER merge multiple questions into one.\n");
    sb.append("3. Each detected question must be a separate object inside the questions array.\n");
    sb.append("4. Skip blank pages, irrelevant content, and duplicate questions.\n");
    sb.append("5. If no valid coding questions are detected, return {\"questions\":[]}.\n\n");

    sb.append("For every question generate these fields:\n");

    sb.append("- title\n");
    sb.append("- problemStatement\n");
    sb.append("- inputFormat\n");
    sb.append("- outputFormat\n");
    sb.append("- constraints\n");
    sb.append("- sampleInput\n");
    sb.append("- sampleOutput\n");
    sb.append("- explanation\n");
    sb.append("- starterCode\n");
    sb.append("- expectedSolution\n");
    sb.append("- difficulty\n");
    sb.append("- marks\n");
    sb.append("- testCases\n\n");

    sb.append("IMPORTANT JSON RULES:\n");
    sb.append("1. Return ONLY valid JSON.\n");
    sb.append("2. Do NOT use markdown or code fences.\n");
    sb.append("3. The response MUST be a JSON object with a 'questions' array.\n");
    sb.append("4. All strings must be valid JSON strings.\n");
    sb.append("5. Escape every double quote inside Java code as \\\".\n");
    sb.append("6. Represent new lines inside code using \\n.\n");
    sb.append("7. Never place raw newlines inside a JSON string.\n");
    sb.append("8. Never leave a JSON string unterminated.\n");
    sb.append("9. Make sure the complete response can be parsed by a standard JSON parser.\n\n");

    sb.append("Each test case must contain:\n");
    sb.append("- input\n");
    sb.append("- expectedOutput\n");
    sb.append("- weight (integer)\n");
    sb.append("- sample (boolean)\n\n");

    sb.append("Generate at least 3 and at most 5 test cases for each question.\n");
    sb.append("The first test case should have sample=true.\n");
    sb.append("difficulty must be \"").append(difficulty).append("\".\n");
    sb.append("marks must be ").append(marks).append(".\n\n");

    sb.append("Return this exact JSON structure:\n");

    sb.append("{\n");
    sb.append("  \"questions\": [\n");
    sb.append("    {\n");
    sb.append("      \"title\": \"Question title\",\n");
    sb.append("      \"problemStatement\": \"Problem description\",\n");
    sb.append("      \"inputFormat\": \"Input format\",\n");
    sb.append("      \"outputFormat\": \"Output format\",\n");
    sb.append("      \"constraints\": \"Constraints\",\n");
    sb.append("      \"sampleInput\": \"Sample input\",\n");
    sb.append("      \"sampleOutput\": \"Sample output\",\n");
    sb.append("      \"explanation\": \"Solution explanation\",\n");
    sb.append("      \"starterCode\": \"Java starter code with escaped quotes and newlines\",\n");
    sb.append("      \"expectedSolution\": \"Complete Java solution with escaped quotes and newlines\",\n");
    sb.append("      \"difficulty\": \"").append(difficulty).append("\",\n");
    sb.append("      \"marks\": ").append(marks).append(",\n");
    sb.append("      \"testCases\": [\n");
    sb.append("        {\"input\": \"5\", \"expectedOutput\": \"25\", \"weight\": 1, \"sample\": true},\n");
    sb.append("        {\"input\": \"10\", \"expectedOutput\": \"100\", \"weight\": 2, \"sample\": false},\n");
    sb.append("        {\"input\": \"15\", \"expectedOutput\": \"225\", \"weight\": 2, \"sample\": false}\n");
    sb.append("      ]\n");
    sb.append("    }\n");
    sb.append("  ]\n");
    sb.append("}\n\n");

    sb.append("Return ONLY the JSON object. Nothing before or after it.");

    return sb.toString();
}
}
