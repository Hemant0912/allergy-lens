package com.allergylens.backend.controller;

import com.allergylens.backend.dto.request.LoginRequest;
import com.allergylens.backend.dto.response.ApiResponse;
import com.allergylens.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ApiResponse login(@Valid @RequestBody LoginRequest request){

    authService.login(request);

    return new ApiResponse("OTP Sent Successfully. Use 123456");

  }

}