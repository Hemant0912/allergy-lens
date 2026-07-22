package com.allergylens.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProfileRequest {

  @NotBlank
  private String name;

  @NotBlank
  private String allergies;

  @NotBlank
  private String diet;
}