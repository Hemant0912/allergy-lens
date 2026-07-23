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
2. If the ingredient list is clearly visible, extract ONLY the ingredients that are explicitly written on the packaging.
3. Never add extra ingredients that are not written on the label.
4. Only infer ingredients if the ingredient list is completely missing or unreadable.
5. If only part of the ingredient list is visible, return only the visible ingredients and do not guess the remaining ones.
6. Compare the identified or inferred ingredients with the user's allergies.
7. Analyze every ingredient and determine whether it is safe for the user.
8. Identify which of the user's allergies are triggered.
9. For every ingredient, provide:
   - The original ingredient name exactly as written on the packaging.
   - A simple everyday name that a non-technical user can understand.
   - Whether the ingredient is safe for the user (true/false).
10. Determine whether the product is safe.
11. Assign a risk level from 1 to 10.

1 = Completely safe
10 = Extremely dangerous

12. Write a short summary (maximum 2 sentences).
13. Give a practical recommendation to the user.
14. Estimate a health score between 0 and 100.
15. Explain the health score in 3-4 short bullet points.
16. If the product is unsafe, suggest 2-3 realistic alternative packaged food products that are less likely to contain the user's allergens.
17. If no suitable alternatives can be confidently suggested, return an empty array.
18. If the product is safe, return an empty array for alternativeProducts.
19. Give your confidence in this analysis using ONLY:
    HIGH
    MEDIUM
    LOW

Rules:
- Return ONLY valid JSON.
- Do NOT wrap the response in markdown.
- Do NOT include ```json.
- Do NOT explain your reasoning.
- If the product name cannot be identified, return "Unknown Product".
- Only infer ingredients when the ingredient list is completely missing or unreadable.
- Never assume a product is safe just because the ingredient list is missing.
- If there is uncertainty, lower the confidence to MEDIUM or LOW.
- If no alternatives are appropriate, return an empty array.
- If the uploaded image is NOT a packaged food product, do NOT analyze it.
- If the image contains people, animals, vehicles, buildings, documents, electronics, scenery, or anything other than a packaged food product, return exactly this JSON:
- Never invent or assume additional ingredients when an ingredient list is visible.
- The ingredient list must exactly match the text on the package whenever it is readable.
- If only 4 ingredients are visible, return only those 4 ingredients.
- Accuracy is more important than completeness.
- Preserve the ingredient names exactly as written on the packaging whenever they are readable.
- Do not correct spelling, expand abbreviations, or rename ingredients in the ingredients array.
- simpleTerm should be a common everyday name that a non-technical user can understand.
- If no simpler name exists, return the original ingredient name.
{
  "productName": "INVALID_IMAGE",
  "ingredients": [],
  "ingredientAnalysis": [],
  "safe": false,
  "riskLevel":10,
  "summary": "The uploaded image is not a packaged food product.",
  "recommendation": "Please upload a clear image of a packaged food product showing the front label or ingredient list.",
  "alternativeProducts": [],
  "confidence": "LOW",
  "triggeredAllergies": [],
  "nutrition": {
    "calories": "N/A",
    "protein": "N/A",
    "fat": "N/A",
    "carbs": "N/A",
    "sugar": "N/A"
  },
  "healthScore": 0,
  "healthInsights": []
}
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
- riskLevel must be an integer between 1 and 10.
- Never return LOW, MEDIUM or HIGH.
- ingredientAnalysis must include every ingredient.
- simpleTerm should be easy for everyone to understand.
- Examples:
  Milk Solids -> Milk
  Casein -> Milk Protein
  Whey Powder -> Milk
  Sucrose -> Sugar
  Dextrose -> Sugar
  Soy Lecithin -> Soy
  Sodium Chloride -> Salt
  INS950 -> Artificial Sweetener
- status=true means safe for this user.
- status=false means unsafe for this user.
Return exactly this JSON format:

{
  "productName": "string",
  "ingredients": [
    "string"
  ],
  "ingredientAnalysis": [
     {
       "ingredient": "Milk Solids",
       "simpleTerm": "Milk",
       "status": false
      }
   ],
  "safe": true,
  "riskLevel":7,
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
  public String chat(String message) {

    String prompt = """
You are AllergyLens AI, a food allergy assistant.

Answer the user's food-related question in a friendly, concise, conversational single paragraph.

Rules:
- Answer only food, nutrition, ingredient, allergy, diet, and food safety questions.
- If the user mentions allergies or dietary preferences, consider them.
- If allergy or diet information is required but missing, politely ask a follow-up question instead of guessing.
- Prioritize food safety.
- If you are unsure, advise the user to verify the ingredient label.
- Do not answer unrelated questions like programming, politics, hacking, etc.
- Keep the response under 120 words.
- Return the response as a single paragraph.
- Do not use bullet points.
- Do not use markdown formatting.

User Question:
%s
""".formatted(message);

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