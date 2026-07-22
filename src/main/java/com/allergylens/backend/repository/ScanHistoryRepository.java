package com.allergylens.backend.repository;

import com.allergylens.backend.entity.ScanHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {

  List<ScanHistory> findByProfileIdOrderByCreatedAtDesc(Long profileId);

}