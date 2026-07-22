package com.allergylens.backend.service;

import com.allergylens.backend.dto.request.IngredientExplainRequest;
import com.allergylens.backend.dto.response.IngredientExplainResponse;

public interface IngredientService {

  IngredientExplainResponse explainIngredient(
      IngredientExplainRequest request);

}