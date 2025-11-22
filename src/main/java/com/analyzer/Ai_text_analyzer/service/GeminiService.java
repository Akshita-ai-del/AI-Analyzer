package com.analyzer.Ai_text_analyzer.service;

import com.analyzer.Ai_text_analyzer.dto.AnalyzeRequest;
import com.analyzer.Ai_text_analyzer.dto.AnalyzeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.url}")
    private String geminiUrl;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    public AnalyzeResponse analyze(AnalyzeRequest req) {

        if (req.getText() == null || req.getText().isBlank()) {
            throw new RuntimeException("Text cannot be empty");
        }

        try {
            String prompt = buildPrompt(req.getText());
            String body = buildRequestBody(prompt);
            String response = callGemini(body);
            String json = extractJson(response);  // cleaned JSON
            return parseResponse(json);

        } catch (Exception e) {
            throw new RuntimeException("Gemini API Error: " + e.getMessage());
        }
    }

    private String buildPrompt(String text) {
        return """
                Analyze this text and return a JSON object with:
                sentiment, summary, keywords, emotion, wordcount, charcount.

                Output must be strictly JSON only.

                Text:
                """ + text;
    }

    private String buildRequestBody(String prompt) throws Exception {

        var root = mapper.createObjectNode();
        var contents = root.putArray("contents");

        var user = contents.addObject();
        user.put("role", "user");

        var parts = user.putArray("parts");
        var part = parts.addObject();
        part.put("text", prompt);

        return mapper.writeValueAsString(root);
    }

    private String callGemini(String body) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(geminiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new RuntimeException("API returned: " + res.statusCode() + " - " + res.body());
        }

        return res.body();
    }

    private String extractJson(String response) throws Exception {

        JsonNode root = mapper.readTree(response);

        String raw = root.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText()
                .trim();

        // Clean markdown backticks (Gemini sometimes adds them)
        raw = raw.replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .trim();

        return raw;
    }

    private AnalyzeResponse parseResponse(String json) throws Exception {

        JsonNode node = mapper.readTree(json);

        List<String> keywords = new ArrayList<>();
        if (node.has("keywords") && node.get("keywords").isArray()) {
            node.get("keywords").forEach(k -> keywords.add(k.asText()));
        }

        return new AnalyzeResponse(
                node.path("sentiment").asText(""),
                node.path("summary").asText(""),
                keywords,
                node.path("emotion").asText(""),
                node.path("wordcount").asInt(0),
                node.path("charcount").asInt(0)
        );
    }
}
