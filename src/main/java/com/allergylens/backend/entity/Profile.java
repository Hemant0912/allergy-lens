package com.allergylens.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  // Example: "Milk,Peanuts,Egg"
  @Column(length = 1000)
  private String allergies;

  private String diet;

  private LocalDateTime createdAt;
}