package com.allergylens.backend.controller;

import com.allergylens.backend.dto.response.DashboardResponse;
import com.allergylens.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/{profileId}")
  public DashboardResponse getDashboard(@PathVariable Long profileId) {
    return dashboardService.getDashboard(profileId);
  }
}