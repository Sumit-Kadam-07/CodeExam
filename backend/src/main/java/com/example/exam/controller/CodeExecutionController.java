package com.example.exam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exam.dto.CodeCompileRequest;
import com.example.exam.dto.CodeExecutionResponse;
import com.example.exam.dto.CodeRunRequest;
import com.example.exam.service.CodeExecutionService;

@RestController
@RequestMapping("/api/code")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/compile")
    public ResponseEntity<CodeExecutionResponse> compile(@RequestBody CodeCompileRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest().body(CodeExecutionResponse.compilationFailed("Invalid request."));
        }

        CodeExecutionService.InternalExecutionResult r = codeExecutionService.compile(req.getLanguage(), req.getSourceCode());
        CodeExecutionResponse resp = new CodeExecutionResponse();
        resp.setSuccess(r.isSuccess());
        resp.setCompilationError(r.getCompilationError());
        resp.setCompilationOutput(r.getCompilationOutput());
        resp.setStdout(null);
        resp.setRuntimeError(null);
        resp.setExecutionTimeMs(r.getExecutionTimeMs());

        if (r.isCompiled() && (resp.getCompilationError() == null || resp.getCompilationError().isBlank())) {
            return ResponseEntity.ok(resp);
        }
        return ResponseEntity.badRequest().body(resp);
    }

    @PostMapping("/run")
    public ResponseEntity<CodeExecutionResponse> run(@RequestBody CodeRunRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest().body(new CodeExecutionResponse(false, null, "Invalid request", null, "Invalid request", 0L));
        }

        CodeExecutionService.InternalExecutionResult r = codeExecutionService.run(req.getLanguage(), req.getSourceCode(), req.getInput());

        CodeExecutionResponse resp = new CodeExecutionResponse();
        resp.setSuccess(r.isSuccess());
        resp.setCompilationError(r.getCompilationError());
        resp.setCompilationOutput(r.getCompilationOutput());
        resp.setStdout(r.getStdout());
        resp.setRuntimeError(r.getRuntimeError());
        resp.setExecutionTimeMs(r.getExecutionTimeMs());

        return ResponseEntity.ok(resp);
    }
}

