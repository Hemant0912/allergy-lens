package com.allergylens.backend.service.impl;

import com.allergylens.backend.service.GeminiService;
import java.io.IOException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

  private final RestClient restClient;

  @Value("${gemini.api.key}")
  private String apiKey;

  private static final String GEMINI_URL =
      "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

  @Override
  public String analyzeFoodLabel(MultipartFile image, String allergies) {

    try {

      System.out.println("Gemini API Key Loaded: " + !apiKey.isBlank());

      String base64Image = Base64.getEncoder()
          .encodeToString(image.getBytes());

      System.out.println(base64Image.substring(0, 50));

      String prompt = """
You are a food allergy expert.

Analyze the food label image.

User allergies:
%s

Return ONLY valid JSON.

{
  "ingredients": [],
  "dangerousIngredients": [],
  "safe": true,
  "riskLevel": "LOW",
  "summary": ""
}
""".formatted(allergies);

      String requestBody = """
{
  "contents": [
    {
      "parts": [
        {
          "text": %s
        },
        {
          "inlineData": {
            "mimeType": %s,
            "data": %s
          }
        }
      ]
    }
  ]
}
""".formatted(
          toJson(prompt),
          toJson(image.getContentType()),
          toJson(base64Image)
      );

      String response = restClient.post()
          .uri(GEMINI_URL + apiKey)
          .header("Content-Type", "application/json")
          .body(requestBody)
          .retrieve()
          .body(String.class);

      return response;

    } catch (IOException e) {
      throw new RuntimeException("Failed to read image", e);
    }
  }
  private String toJson(String value) {
    if (value == null) {
      return "null";
    }

    return "\"" +
        value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        + "\"";
  }
}