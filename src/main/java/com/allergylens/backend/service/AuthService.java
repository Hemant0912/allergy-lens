package com.allergylens.backend.service;

import com.allergylens.backend.dto.request.LoginRequest;

public interface AuthService {

  void login(LoginRequest request);

}