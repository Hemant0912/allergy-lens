package com.allergylens.backend.service;

import com.allergylens.backend.dto.request.ChatRequest;
import com.allergylens.backend.dto.response.ChatResponse;

public interface ChatService {

  ChatResponse chat(ChatRequest request);

}