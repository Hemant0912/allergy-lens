package com.allergylens.backend.service.impl;

import com.allergylens.backend.dto.response.IngredientExplainResponse;
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
11. Estimate a health score between 0 and 100.
12. Explain the health score in 3-4 short bullet points.
13. If the product is unsafe, suggest 2-3 safer alternative products.
14. If the product is safe, return an empty array for alternativeProducts.
15. Give your confidence in this analysis using ONLY:
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

Nutrition Analysis Rules:
- If the Nutrition Facts panel is visible, extract the nutrition values exactly.
- If the Nutrition Facts panel is not visible, estimate approximate nutrition values based on the identified product.
- Append "(estimated)" to every estimated value.
- Never invent precise nutrition values.
- If ANY nutrition value is estimated, confidence MUST be LOW.
- Never return MEDIUM or HIGH confidence when nutrition values are estimated.
- healthScore must be an integer between 0 and 100.
- Estimate the healthScore based on the estimated or extracted nutrition values.
- Base the healthScore on sugar, saturated fat, sodium, protein, fiber (if available), and overall processing level.
- The healthScore is independent of the user's allergies.

Return exactly this JSON format:

{
  "productName": "string",
  "ingredients": [
    "string"
  ],
  "dangerousIngredients": [
    "string"
  ],
  "safe": true,
  "riskLevel": "LOW | MEDIUM | HIGH",
  "summary": "string",
  "recommendation": "string",
  "alternativeProducts": [
    "string"
  ],
  "confidence": "HIGH | MEDIUM | LOW",
  "triggeredAllergies": [
    "string"
  ],
  "nutrition": {
    "calories": "string",
    "protein": "string",
    "fat": "string",
    "carbs": "string",
    "sugar": "string"
  },
"healthScore": 74,
"healthInsights": [
  "Good source of protein",
  "Low sugar",
  "Moderate fat",
  "Suitable for regular consumption"
]
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

Answer the user's question in a friendly, concise, conversational single paragraph.

Rules:
- Prioritize food safety.
- Consider the user's allergies.
- Consider the user's diet.
- If you are unsure, advise the user to verify the ingredient label.
- Do not answer unrelated questions like programming, politics, hacking, etc.
- Keep the response under 120 words.
- Return the response as a single paragraph.
- Do not use bullet points.
- Do not use newline characters.
- Do not use markdown formatting such as **bold** or lists.

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
  @Override
  public IngredientExplainResponse explainIngredient(String ingredient) {

    String prompt = """
You are a food ingredient expert.

Explain the following food ingredient:

"%s"

Return ONLY valid JSON.

{
  "ingredient": "Milk Solids",
  "description": "Milk solids are concentrated milk proteins commonly used in processed foods.",
  "commonUses": [
    "Chocolate",
    "Ice Cream",
    "Biscuits"
  ],
  "allergyWarnings": [
    "Milk Allergy",
    "Lactose Intolerance"
  ],
  "recommendation": "Avoid if you have a milk allergy."
}

Rules:
- Return ONLY valid JSON.
- Do NOT wrap the response in markdown.
- Do NOT include ```json.
- Description must be under 25 words.
- Recommendation must be under 15 words.
- Maximum 3 common uses.
- commonUses should contain only short names, not sentences.
- allergyWarnings must contain ONLY allergy names.
- Do not write complete sentences inside allergyWarnings.
- If there are no allergy warnings, return an empty array.
- Do not return explanation outside JSON.
- If the ingredient is generally safe for most people, return an empty allergyWarnings array.
- If the ingredient is unknown, state that clearly in the description.
- Avoid absolute statements like "safe for everyone".
- Use phrases like "generally safe for most people".
- If the ingredient is not a food ingredient, set commonUses to an empty array.
- If the ingredient is a food additive (e.g., E330, Xanthan Gum, Soy Lecithin), explain its purpose in food.
""".formatted(ingredient);

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

      String aiResponse = root.path("candidates")
          .get(0)
          .path("content")
          .path("parts")
          .get(0)
          .path("text")
          .asText();

      aiResponse = aiResponse
          .replace("```json", "")
          .replace("```", "")
          .trim();

      return objectMapper.readValue(
          aiResponse,
          IngredientExplainResponse.class
      );

    } catch (Exception e) {
      throw new RuntimeException("Failed to process ingredient explanation", e);
    }
  }
}