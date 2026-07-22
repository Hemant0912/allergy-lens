package com.allergylens.backend.controller;

import com.allergylens.backend.dto.request.IngredientExplainRequest;
import com.allergylens.backend.dto.response.IngredientExplainResponse;
import com.allergylens.backend.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingredient")
@RequiredArgsConstructor
public class IngredientController {

  private final IngredientService ingredientService;

  @PostMapping("/explain")
  public IngredientExplainResponse explain(
      @RequestBody IngredientExplainRequest request) {

    return ingredientService.explainIngredient(request);
  }
}