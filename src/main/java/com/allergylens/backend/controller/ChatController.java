package com.allergylens.backend.controller;

import com.allergylens.backend.dto.request.ChatRequest;
import com.allergylens.backend.dto.response.ChatResponse;
import com.allergylens.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @PostMapping
  public ChatResponse chat(@RequestBody ChatRequest request) {
    return chatService.chat(request);
  }
}