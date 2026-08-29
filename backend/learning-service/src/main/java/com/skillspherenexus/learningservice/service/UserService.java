package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.UserLoginRequestDTO;
import com.skillspherenexus.learningservice.dto.UserResponseDTO;
import com.skillspherenexus.learningservice.enums.UserRole;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDTO login(UserLoginRequestDTO request);
    UserResponseDTO getUserById(UUID userId);
    List<UserResponseDTO> getUsers(UserRole role);
}
