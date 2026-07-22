package com.allergylens.backend.service.impl;

import com.allergylens.backend.dto.request.CreateProfileRequest;
import com.allergylens.backend.dto.response.ProfileResponse;
import com.allergylens.backend.entity.Profile;
import com.allergylens.backend.repository.ProfileRepository;
import com.allergylens.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

  private final ProfileRepository profileRepository;

  @Override
  public ProfileResponse createProfile(CreateProfileRequest request) {

    Profile profile = Profile.builder()
        .name(request.getName())
        .allergies(request.getAllergies())
        .diet(request.getDiet())
        .createdAt(LocalDateTime.now())
        .build();

    Profile savedProfile = profileRepository.save(profile);

    return ProfileResponse.builder()
        .id(savedProfile.getId())
        .name(savedProfile.getName())
        .allergies(savedProfile.getAllergies())
        .diet(savedProfile.getDiet())
        .build();
  }
  @Override
  public ProfileResponse getProfile(Long id) {

    Profile profile = profileRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Profile not found"));

    return ProfileResponse.builder()
        .id(profile.getId())
        .name(profile.getName())
        .allergies(profile.getAllergies())
        .diet(profile.getDiet())
        .build();
  }
}