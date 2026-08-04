package com.example.exam.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.exam.dto.AiGenerateResponse;
import com.example.exam.model.Question;
import com.example.exam.service.AdminService;
import com.example.exam.service.AiQuestionService;
import com.example.exam.service.FileExtractionService;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasRole('ADMIN')")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private final FileExtractionService fileExtractionService;
    private final AiQuestionService aiQuestionService;
    private final AdminService adminService;

    public FileUploadController(FileExtractionService fileExtractionService,
                                 AiQuestionService aiQuestionService,
                                 AdminService adminService) {
        this.fileExtractionService = fileExtractionService;
        this.aiQuestionService = aiQuestionService;
        this.adminService = adminService;
    }

    @PostMapping("/upload")
    @Transactional
    public ResponseEntity<Map<String, Object>> uploadAndGenerate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("examId") Long examId,
            @RequestParam(value = "language", defaultValue = "Java") String language,
            @RequestParam(value = "difficulty", defaultValue = "Medium") String difficulty,
            @RequestParam(value = "marks", defaultValue = "10") int marks) {

        logger.info("API: File upload and generate - file={}, lang={}, diff={}, examId={}",
            file.getOriginalFilename(), language, difficulty, examId);

        try {
            if (file.isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "No file uploaded.");
                return ResponseEntity.badRequest().body(err);
            }

            if (!fileExtractionService.isSupportedFileType(file.getContentType(), file.getOriginalFilename())) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                    err.put("message", "Unsupported file type. Supported: PDF, DOC, DOCX, TXT, MD");
                return ResponseEntity.badRequest().body(err);
            }

            // Step 1: Extract text from file
            String extractedText = fileExtractionService.extractText(file);
            logger.info("Extracted {} characters from {}", extractedText.length(), file.getOriginalFilename());

            if (extractedText == null || extractedText.trim().isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Could not extract text from the uploaded file. The file may be empty or contain only images.");
                return ResponseEntity.badRequest().body(err);
            }

            // Step 2: Send extracted text to AI to generate multiple questions
            List<AiGenerateResponse> aiQuestions = aiQuestionService.generateMultipleQuestions(extractedText, language, difficulty, marks);
            logger.info("AI generated {} questions from uploaded file", aiQuestions.size());

            if (aiQuestions.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("savedCount", 0);
                response.put("questionIds", new ArrayList<>());
                response.put("failedCount", 0);
                response.put("skipped", 0);
                response.put("message", "No valid coding questions detected in the uploaded file.");
                return ResponseEntity.ok(response);
            }

            // Step 3: Save each question to the exam
            List<Long> savedQuestionIds = new ArrayList<>();
            int failedCount = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < aiQuestions.size(); i++) {
                AiGenerateResponse aiQ = aiQuestions.get(i);
                try {
                    Question question = new Question();
                    question.setTitle(aiQ.getTitle() != null ? aiQ.getTitle() : "Question " + (i + 1));
                    question.setProblemStatement(aiQ.getProblemStatement() != null ? aiQ.getProblemStatement() : "");
                    question.setLanguage(language);
                    question.setDifficulty(aiQ.getDifficulty() != null ? aiQ.getDifficulty() : difficulty);
                    question.setMarks(aiQ.getMarks() > 0 ? aiQ.getMarks() : marks);
                    question.setStarterCode(aiQ.getStarterCode() != null ? aiQ.getStarterCode() : "");
                    question.setExecutionTimeout(5000);
                    question.setMemoryLimit(256);
                    question.setInputFormat(aiQ.getInputFormat());
                    question.setOutputFormat(aiQ.getOutputFormat());
                    question.setConstraints(aiQ.getConstraints());
                    question.setSampleInput(aiQ.getSampleInput());
                    question.setSampleOutput(aiQ.getSampleOutput());
                    question.setExplanation(aiQ.getExplanation());
                    question.setExpectedSolution(aiQ.getExpectedSolution());

                    // Convert test cases to arrays
                    String[] testCaseInputs = null;
                    String[] testCaseExpectedOutputs = null;
                    int[] testCaseWeights = null;
                    String[] testCaseIsSample = null;

                    if (aiQ.getTestCases() != null && !aiQ.getTestCases().isEmpty()) {
                        int tcCount = aiQ.getTestCases().size();
                        testCaseInputs = new String[tcCount];
                        testCaseExpectedOutputs = new String[tcCount];
                        testCaseWeights = new int[tcCount];
                        testCaseIsSample = new String[tcCount];

                        for (int j = 0; j < tcCount; j++) {
                            var tc = aiQ.getTestCases().get(j);
                            testCaseInputs[j] = tc.getInput() != null ? tc.getInput() : "";
                            testCaseExpectedOutputs[j] = tc.getExpectedOutput() != null ? tc.getExpectedOutput() : "";
                            testCaseWeights[j] = tc.getWeight() > 0 ? tc.getWeight() : 1;
                            testCaseIsSample[j] = tc.isSample() ? "true" : "false";
                        }
                    }

                    // Validate and save
                    String validationError = adminService.validateQuestionForSubmission(
                        question, testCaseInputs, testCaseExpectedOutputs, testCaseWeights, testCaseIsSample);
                    if (validationError != null) {
                        logger.warn("Skipping question {} due to validation error: {}", i + 1, validationError);
                        failedCount++;
                        errors.add("Question " + (i + 1) + ": " + validationError);
                        continue;
                    }

                    AdminService.QuestionCreationOutcome outcome = adminService.addQuestionWithArrays(
                        question, examId, testCaseInputs, testCaseExpectedOutputs, testCaseWeights, testCaseIsSample);
                    savedQuestionIds.add(outcome.questionId);
                    logger.info("Saved question {} (id={}) from uploaded file", i + 1, outcome.questionId);

                } catch (Exception e) {
                    logger.error("Failed to save question {} from uploaded file", i + 1, e);
                    failedCount++;
                    errors.add("Question " + (i + 1) + ": " + e.getMessage());
                }
            }

            // Step 4: Return combined response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("savedCount", savedQuestionIds.size());
            response.put("questionIds", savedQuestionIds);
            response.put("failedCount", failedCount);
            response.put("skipped", aiQuestions.size() - savedQuestionIds.size() - failedCount);
            response.put("message", savedQuestionIds.size() + " question(s) created and saved successfully.");
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }
            return ResponseEntity.ok(response);

        } catch (UnsupportedOperationException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            logger.error("File upload processing failed", e);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Processing failed: " + e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }
}
