package com.allergylens.backend.service;

import com.allergylens.backend.dto.request.CreateProfileRequest;
import com.allergylens.backend.dto.response.ProfileResponse;

public interface ProfileService {

  ProfileResponse createProfile(CreateProfileRequest request);

}