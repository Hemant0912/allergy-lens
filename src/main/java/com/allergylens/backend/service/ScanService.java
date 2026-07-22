package com.allergylens.backend.service;

import com.allergylens.backend.dto.response.ScanHistoryListResponse;
import com.allergylens.backend.dto.response.ScanHistoryResponse;
import com.allergylens.backend.dto.response.ScanResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ScanService {

  ScanResponse scan(Long profileId, MultipartFile image);

  ScanHistoryListResponse getScanHistory(Long profileId);

}