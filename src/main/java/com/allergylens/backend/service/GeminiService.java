package com.allergylens.backend.service;

import com.allergylens.backend.dto.response.IngredientExplainResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface GeminiService {

  String analyzeFoodLabel(List<MultipartFile> images, String allergies);

  String chat(String message, String allergies, String diet);

  IngredientExplainResponse explainIngredient(String ingredient);
}