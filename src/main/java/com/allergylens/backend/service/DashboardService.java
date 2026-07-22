package com.allergylens.backend.service;

import com.allergylens.backend.dto.response.DashboardResponse;

public interface DashboardService {

  DashboardResponse getDashboard(Long profileId);

}