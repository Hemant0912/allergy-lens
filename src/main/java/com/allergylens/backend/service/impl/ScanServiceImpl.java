package com.allergylens.backend.service.impl;

import com.allergylens.backend.dto.response.BatchScanResponse;
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
import java.util.ArrayList;
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

  private final GeminiService geminiService;
  private final ObjectMapper objectMapper;

  @Override
  public ScanResponse scan(List<MultipartFile> images, String allergies){

    try {

      // validate images
      if (images == null || images.isEmpty()) {
        throw new RuntimeException("Please upload at least one image.");
      }

      for (MultipartFile image : images) {
        if (image.isEmpty()) {
          throw new RuntimeException("One of the uploaded images is empty.");
        }

        if (image.getContentType() == null ||
            !image.getContentType().startsWith("image/")) {
          throw new RuntimeException("Only image files are allowed.");
        }
      }

      // validate profile

      String geminiJson = geminiService.analyzeFoodLabel(
          images,
          allergies
      );

      ScanResponse response =
          objectMapper.readValue(geminiJson, ScanResponse.class);
      if ("INVALID_IMAGE".equals(response.getProductName())) {
        throw new RuntimeException(
            "Invalid image. Please upload a clear image of a packaged food product.");
      }

      System.out.println("Triggered Allergies = " + response.getTriggeredAllergies());
      return response;

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to scan image", e);
    }
  }
}