package com.allergylens.backend.controller;

import com.allergylens.backend.dto.request.CreateProfileRequest;
import com.allergylens.backend.dto.response.ProfileResponse;
import com.allergylens.backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProfileResponse createProfile(@Valid @RequestBody CreateProfileRequest request) {
    return profileService.createProfile(request);
  }

  @GetMapping("/{id}")
  public ProfileResponse getProfile(@PathVariable Long id) {
    return profileService.getProfile(id);
  }
}