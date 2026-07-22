package com.allergylens.backend.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {

  private Long id;
  private String name;
  private String allergies;
  private String diet;
  private LocalDateTime createdAt;
}