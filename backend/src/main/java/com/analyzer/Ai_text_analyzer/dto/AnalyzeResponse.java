package com.analyzer.Ai_text_analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AnalyzeResponse {
    private String sentiment;
    private String summary;
    private List<String> keywords;
    private String emotion;
    private int wordcount;
    private int charcount;
}
