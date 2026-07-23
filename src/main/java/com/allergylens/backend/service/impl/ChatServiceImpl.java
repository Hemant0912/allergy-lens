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

  private final GeminiService geminiService;

  @Override
  public ChatResponse chat(ChatRequest request) {

    String reply = geminiService.chat(request.getMessage());

    return ChatResponse.builder()
        .reply(reply)
        .build();
  }
}