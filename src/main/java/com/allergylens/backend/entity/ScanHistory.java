package com.allergylens.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scan_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long profileId;

  @Column(length = 500)
  private String productName;

  @Column(length = 3000)
  private String recommendation;

  @Column(length = 3000)
  private String alternativeProducts;

  private String confidence;

  private Integer healthScore;

  @Column(length = 5000)
  private String ingredients;

  @Column(length = 2000)
  private String dangerousIngredients;

  private Boolean safe;

  private String riskLevel;

  @Column(length = 3000)
  private String summary;

  private LocalDateTime createdAt;

  @Column(length = 1000)
  private String triggeredAllergies;

}