package com.allergylens.backend.controller;

import com.allergylens.backend.dto.response.ScanHistoryListResponse;
import com.allergylens.backend.dto.response.ScanHistoryResponse;
import com.allergylens.backend.dto.response.ScanResponse;
import com.allergylens.backend.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/scan")
@RequiredArgsConstructor
public class ScanController {

  private final ScanService scanService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ScanResponse scan(
      @RequestParam Long profileId,
      @RequestParam List<MultipartFile> images
  ) {
    return scanService.scan(profileId, images);
  }

  @GetMapping("/history/{profileId}")
  public ScanHistoryListResponse getScanHistory(
      @PathVariable Long profileId
  ) {
    return scanService.getScanHistory(profileId);
  }
}