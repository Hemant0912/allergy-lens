package com.allergylens.backend.service.impl;

import com.allergylens.backend.dto.response.ProfileResponse;
import com.allergylens.backend.dto.response.ScanHistoryListResponse;
import com.allergylens.backend.dto.response.ScanResponse;
import com.allergylens.backend.entity.Profile;
import com.allergylens.backend.entity.ScanHistory;
import com.allergylens.backend.repository.ProfileRepository;
import com.allergylens.backend.repository.ScanHistoryRepository;
import com.allergylens.backend.service.GeminiService;
import com.allergylens.backend.service.ScanService;
import java.time.LocalDateTime;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;
import com.allergylens.backend.dto.response.ScanHistoryResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {

  private final ProfileRepository profileRepository;
  private final ScanHistoryRepository scanHistoryRepository;
  private final GeminiService geminiService;
  private final ObjectMapper objectMapper;

  @Override
  public ScanResponse scan(Long profileId, MultipartFile image) {

    try {

      // validate image
      if (image == null || image.isEmpty()) {
        throw new RuntimeException("Please upload an image.");
      }

      if (image.getContentType() == null ||
          !image.getContentType().startsWith("image/")) {
        throw new RuntimeException("Only image files are allowed.");
      }

      // validate profile
      Profile profile = profileRepository.findById(profileId)
          .orElseThrow(() -> new RuntimeException("Profile not found"));

      String geminiJson = geminiService.analyzeFoodLabel(
          image,
          profile.getAllergies()
      );

      ScanResponse response =
          objectMapper.readValue(geminiJson, ScanResponse.class);

      ScanHistory history = ScanHistory.builder()
          .profileId(profileId)
          .ingredients(response.getIngredients() == null
              ? ""
              : String.join(", ", response.getIngredients()))
          .dangerousIngredients(
              response.getDangerousIngredients() == null
                  ? ""
                  : String.join(", ", response.getDangerousIngredients())
          )
          .safe(response.isSafe())
          .riskLevel(response.getRiskLevel())
          .summary(response.getSummary())
          .createdAt(LocalDateTime.now())
          .build();

      scanHistoryRepository.save(history);

      return response;

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to scan image", e);
    }
  }

  @Override
  public ScanHistoryListResponse getScanHistory(Long profileId) {

    Profile profile = profileRepository.findById(profileId)
        .orElseThrow(() -> new RuntimeException("Profile not found"));

    List<ScanHistoryResponse> history = scanHistoryRepository
        .findByProfileIdOrderByCreatedAtDesc(profileId)
        .stream()
        .map(scan -> ScanHistoryResponse.builder()
            .id(scan.getId())
            .ingredients(
                scan.getIngredients() == null ||
                    scan.getIngredients().isBlank()
                    ? List.of()
                    : Arrays.stream(scan.getIngredients().split(","))
                        .map(String::trim)
                        .toList()
            )
            .dangerousIngredients(
                scan.getDangerousIngredients() == null ||
                    scan.getDangerousIngredients().isBlank()
                    ? List.of()
                    : Arrays.stream(scan.getDangerousIngredients().split(","))
                        .map(String::trim)
                        .toList()
            )
            .safe(scan.getSafe())
            .riskLevel(scan.getRiskLevel())
            .summary(scan.getSummary())
            .createdAt(scan.getCreatedAt())
            .build())
        .toList();

    ProfileResponse profileResponse = ProfileResponse.builder()
        .id(profile.getId())
        .name(profile.getName())
        .allergies(profile.getAllergies())
        .diet(profile.getDiet())
        .createdAt(profile.getCreatedAt())
        .build();

    return ScanHistoryListResponse.builder()
        .profile(profileResponse)
        .history(history)
        .build();
  }
}