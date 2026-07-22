package com.allergylens.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface GeminiService {

  String analyzeFoodLabel(MultipartFile image, String allergies);

}