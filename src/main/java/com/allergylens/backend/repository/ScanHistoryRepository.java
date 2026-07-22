package com.allergylens.backend.repository;

import com.allergylens.backend.entity.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {
}