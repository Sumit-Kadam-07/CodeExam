package com.example.exam.ai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.exam.config.AiQuestionProperties;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class AiClient {

    private static final Logger logger = LoggerFactory.getLogger(AiClient.class);
    private final AiQuestionProperties properties;
    private final Gson gson = new Gson();

    public AiClient(AiQuestionProperties properties) {
        this.properties = properties;
    }

    public String callApi(String prompt) {
        String provider = properties.getProvider();
        logger.info("Calling AI provider: {} at {}", provider, properties.getApiUrl());

        try {
            if ("ollama".equalsIgnoreCase(provider)) {
                return callOllama(prompt);
            } else if ("openai".equalsIgnoreCase(provider)) {
                return callOpenAiCompatible(prompt);
            } else {
                return callOllama(prompt);
            }
        } catch (Exception e) {
            logger.error("AI API call failed", e);
            throw new RuntimeException("AI generation failed: " + e.getMessage());
        }
    }

    private String callOllama(String prompt) throws Exception {
        URL url = URI.create(properties.getApiUrl()).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);

            JsonObject body = new JsonObject();
            body.addProperty("model", properties.getModel());
            body.addProperty("prompt", prompt);
            body.addProperty("stream", false);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(gson.toJson(body).getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                String errText = "";
                if (conn.getErrorStream() != null) {
                    try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                        errText = err.lines().reduce("", String::concat);
                    }
                }
                throw new RuntimeException("Ollama returned " + code + ": " + errText);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String responseText = reader.lines().reduce("", String::concat);
                JsonObject json = JsonParser.parseString(responseText).getAsJsonObject();
                return json.get("response").getAsString();
            }
        } finally {
            conn.disconnect();
        }
    }

    private String callOpenAiCompatible(String prompt) throws Exception {
        URL url = URI.create(properties.getApiUrl()).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            if (properties.getApiKey() != null && !properties.getApiKey().isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + properties.getApiKey());
            }
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);

            JsonObject body = new JsonObject();
            body.addProperty("model", properties.getModel());

            JsonArray messages = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("role", "user");
            msg.addProperty("content", prompt);
            messages.add(msg);
            body.add("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(gson.toJson(body).getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                String errText = "";
                if (conn.getErrorStream() != null) {
                    try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                        errText = err.lines().reduce("", String::concat);
                    }
                }
                throw new RuntimeException("AI API returned " + code + ": " + errText);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String responseText = reader.lines().reduce("", String::concat);
                JsonObject json = JsonParser.parseString(responseText).getAsJsonObject();
                return json.getAsJsonArray("choices")
                           .get(0).getAsJsonObject()
                           .getAsJsonObject("message")
                           .get("content").getAsString();
            }
        } finally {
            conn.disconnect();
        }
    }
}
