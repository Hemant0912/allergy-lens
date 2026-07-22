package com.allergylens.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScanHistoryListResponse {

  private ProfileResponse profile;

  private List<ScanHistoryResponse> history;
}