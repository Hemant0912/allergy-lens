package com.allergylens.backend.service;

import com.allergylens.backend.dto.response.BatchScanResponse;
import com.allergylens.backend.dto.response.ScanHistoryListResponse;
import com.allergylens.backend.dto.response.ScanResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ScanService {

  ScanResponse scan(Long profileId, List<MultipartFile> images);

  ScanHistoryListResponse getScanHistory(Long profileId);

  BatchScanResponse batchScan(Long profileId, List<MultipartFile> images);
}