package com.allergylens.backend.service;

import com.allergylens.backend.dto.request.LoginRequest;
import com.allergylens.backend.entity.User;
import com.allergylens.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository repository;

  @Override
  public void login(LoginRequest request) {

    repository.findByPhoneNumber(request.getPhoneNumber())
        .orElseGet(() -> repository.save(
            User.builder()
                .phoneNumber(request.getPhoneNumber())
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build()
        ));

  }
}