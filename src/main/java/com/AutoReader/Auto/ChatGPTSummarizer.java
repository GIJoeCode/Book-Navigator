package com.AutoReader.Auto;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ChatGPTSummarizer {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String summarize(String content) throws IOException {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("OpenAI API key is missing. Please set the environment variable 'OPENAI_API_KEY'.");
        }

        Map<String, Object> json = new HashMap<>();
        json.put("model", "gpt-3.5-turbo");
        json.put("messages", new Object[]{
                new HashMap<String, String>() {{
                    put("role", "system");
                    put("content", "You are a helpful assistant that summarizes content.");
                }},
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", "Summarize the following content:\n" + content);
                }}
        });

        RequestBody body = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                objectMapper.writeValueAsString(json)
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            Map<String, Object> responseBody = objectMapper.readValue(response.body().string(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            return (String) message.get("content");
        }
    }
}
