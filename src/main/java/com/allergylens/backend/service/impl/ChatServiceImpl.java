package com.allergylens.backend.service.impl;

import com.allergylens.backend.dto.request.ChatRequest;
import com.allergylens.backend.dto.response.ChatResponse;
import com.allergylens.backend.entity.Profile;
import com.allergylens.backend.repository.ProfileRepository;
import com.allergylens.backend.service.ChatService;
import com.allergylens.backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final ProfileRepository profileRepository;

  private final GeminiService geminiService;

  @Override
  public ChatResponse chat(ChatRequest request) {

    Profile profile = profileRepository.findById(request.getProfileId())
        .orElseThrow(() -> new RuntimeException("Profile not found"));

    String reply = geminiService.chat(
        request.getMessage(),
        profile.getAllergies(),
        profile.getDiet()
    );

    return ChatResponse.builder()
        .reply(reply)
        .build();
  }
}