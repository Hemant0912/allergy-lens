package com.allergylens.backend.service.impl;

import com.allergylens.backend.dto.request.IngredientExplainRequest;
import com.allergylens.backend.dto.response.IngredientExplainResponse;
import com.allergylens.backend.service.GeminiService;
import com.allergylens.backend.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

  private final GeminiService geminiService;

  @Override
  public IngredientExplainResponse explainIngredient(
      IngredientExplainRequest request) {

    return geminiService.explainIngredient(request.getIngredient());
  }
}