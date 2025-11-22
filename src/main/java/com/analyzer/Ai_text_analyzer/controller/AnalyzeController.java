package com.analyzer.Ai_text_analyzer.controller;

import com.analyzer.Ai_text_analyzer.dto.AnalyzeRequest;
import com.analyzer.Ai_text_analyzer.dto.AnalyzeResponse;
import com.analyzer.Ai_text_analyzer.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AnalyzeController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/analyze")
    public AnalyzeResponse analyze(@RequestBody AnalyzeRequest request) {
        return geminiService.analyze(request);
    }
}
