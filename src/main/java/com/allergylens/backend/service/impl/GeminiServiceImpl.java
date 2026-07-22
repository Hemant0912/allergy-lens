package com.allergylens.backend.service.impl;

import com.allergylens.backend.service.GeminiService;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

  private final RestClient restClient;

  @Value("${gemini.api.key}")
  private String apiKey;

  private final ObjectMapper objectMapper;

  private static final String GEMINI_URL =
      "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=";

  @Override
  public String analyzeFoodLabel(List<MultipartFile> images, String allergies) {

    try {

      System.out.println("Gemini API Key Loaded: " + !apiKey.isBlank());

      String prompt = """
You are an expert Food Safety and Allergy Detection AI.

Analyze the uploaded food label(s) carefully.

The user may upload multiple images of the same product, such as:
- Front of the package
- Back ingredient label
- Nutrition facts

Combine information from ALL uploaded images before making your decision.
If the ingredient list is visible in one image, always prefer that over inferred ingredients.
Treat all uploaded images as belonging to the same product.

The user has the following allergies:
%s

Your task:

1. Identify the product name.
2. Extract ingredients if they are clearly visible on the packaging.
3. If the ingredient list is not visible, infer the likely major ingredients based on the identified food product.
4. Compare the identified or inferred ingredients with the user's allergies.
5. Identify the dangerous ingredients that cause the allergy risk.
6. Identify which of the user's allergies are triggered.
7. Determine whether the product is safe.
8. Assign exactly one risk level:
   LOW
   MEDIUM
   HIGH
9. Write a short summary (maximum 2 sentences).
10. Give a practical recommendation to the user.
11. Suggest 2-3 safer alternative products if the food is unsafe.
12. Give your confidence in this analysis using ONLY:
    HIGH
    MEDIUM
    LOW

Rules:
- Return ONLY valid JSON.
- Do NOT wrap the response in markdown.
- Do NOT include ```json.
- Do NOT explain your reasoning.
- If the product name cannot be identified, return "Unknown Product".
- If the ingredient list is not visible, infer the most likely ingredients based on the product type.
- Never assume a product is safe just because the ingredient list is missing.
- If there is uncertainty, lower the confidence to MEDIUM or LOW.
- If no alternatives are appropriate, return an empty array.

Return exactly this JSON format:

{
  "productName": "",
  "ingredients": [],
  "dangerousIngredients": [],
  "triggeredAllergies": [],
  "safe": true,
  "riskLevel": "LOW",
  "summary": "",
  "recommendation": "",
  "alternativeProducts": [],
  "confidence": "HIGH"
}
""".formatted(allergies == null ? "None" : allergies);

      StringBuilder parts = new StringBuilder();

      parts.append("""
{
  "text": %s
}
""".formatted(toJson(prompt)));

      for (MultipartFile image : images) {

        String base64Image = Base64.getEncoder()
            .encodeToString(image.getBytes());

        parts.append(",");

        parts.append("""
{
  "inlineData": {
    "mimeType": %s,
    "data": %s
  }
}
""".formatted(
            toJson(image.getContentType()),
            toJson(base64Image)
        ));
      }

      String requestBody = """
{
  "contents": [
    {
      "parts": [
        %s
      ]
    }
  ]
}
""".formatted(parts.toString());

      String response = restClient.post()
          .uri(GEMINI_URL + apiKey)
          .header("Content-Type", "application/json")
          .body(requestBody)
          .retrieve()
          .body(String.class);

      JsonNode root = objectMapper.readTree(response);

      String aiResponse = root.path("candidates")
          .get(0)
          .path("content")
          .path("parts")
          .get(0)
          .path("text")
          .asText();

      aiResponse = aiResponse.replace("```json", "")
          .replace("```", "")
          .trim();

      return aiResponse;

    } catch (IOException e) {
      throw new RuntimeException("Failed to process Gemini response", e);
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
  @Override
  public String chat(String message, String allergies, String diet) {

    String prompt = """
You are AllergyLens AI, a food allergy assistant.

User Profile:
- Allergies: %s
- Diet: %s

Answer the user's question in a friendly and concise way.

Rules:
- Prioritize food safety.
- Consider the user's allergies.
- Consider the user's diet.
- If you are unsure, advise the user to verify the ingredient label.
- Do not answer unrelated questions like programming, politics, hacking, etc.
- Keep the response under 120 words.

User Question:
%s
""".formatted(
        allergies == null ? "None" : allergies,
        diet == null ? "None" : diet,
        message
    );

    String requestBody = """
{
  "contents": [
    {
      "parts": [
        {
          "text": %s
        }
      ]
    }
  ]
}
""".formatted(toJson(prompt));

    String response = restClient.post()
        .uri(GEMINI_URL + apiKey)
        .header("Content-Type", "application/json")
        .body(requestBody)
        .retrieve()
        .body(String.class);

    try {

      JsonNode root = objectMapper.readTree(response);

      return root.path("candidates")
          .get(0)
          .path("content")
          .path("parts")
          .get(0)
          .path("text")
          .asText();

    } catch (Exception e) {
      throw new RuntimeException("Failed to process chat response", e);
    }
  }
}