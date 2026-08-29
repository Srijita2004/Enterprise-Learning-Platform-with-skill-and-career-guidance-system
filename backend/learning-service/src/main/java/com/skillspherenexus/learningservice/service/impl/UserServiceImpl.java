package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.UserLoginRequestDTO;
import com.skillspherenexus.learningservice.dto.UserResponseDTO;
import com.skillspherenexus.learningservice.entity.AppUser;
import com.skillspherenexus.learningservice.enums.UserRole;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.AppUserRepository;
import com.skillspherenexus.learningservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.skillspherenexus.learningservice.security.JwtTokenProvider jwtTokenProvider;

    @Override
    public UserResponseDTO login(UserLoginRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Login request is required");
        }

        String fullName = normalizeDisplayName(request.getFullName());
        String normalizedName = fullName.toLowerCase(Locale.ROOT);
        String email = normalizeEmail(request.getEmail());
        UserRole role = request.getRole();

        AppUser accountByEmail = appUserRepository
                .findByNormalizedEmail(email)
                .orElse(null);

        if (accountByEmail != null) {
            validateExistingAccount(accountByEmail, request, role);

            if (!accountByEmail.getFullName().equals(fullName)) {
                accountByEmail.setFullName(fullName);
                accountByEmail.setNormalizedName(normalizedName);
            }

            accountByEmail.setLastLoginAt(LocalDateTime.now());
            return mapToResponse(appUserRepository.save(accountByEmail));
        }

        /*
         * Upgrade an account created by the previous name-and-role login.
         * This keeps the existing user ID, enrollments, progress and certificates.
         */
        AppUser legacyAccount = appUserRepository
                .findByNormalizedNameAndRole(normalizedName, role)
                .filter(user -> user.getNormalizedEmail() == null || user.getNormalizedEmail().isBlank())
                .orElse(null);

        if (legacyAccount != null) {
            if (!Boolean.TRUE.equals(legacyAccount.getActive())) {
                throw new IllegalArgumentException("This user account is inactive");
            }

            legacyAccount.setEmail(email);
            legacyAccount.setNormalizedEmail(email);
            legacyAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            legacyAccount.setLastLoginAt(LocalDateTime.now());
            return mapToResponse(appUserRepository.save(legacyAccount));
        }

        AppUser sameNameAccount = appUserRepository
                .findByNormalizedNameAndRole(normalizedName, role)
                .orElse(null);
        if (sameNameAccount != null) {
            throw new IllegalArgumentException(
                    "An account already exists for this name and role. Use the email originally registered for that account."
            );
        }

        UUID requestedId = request.getPreferredUserId();
        UUID userId = requestedId != null && !appUserRepository.existsById(requestedId)
                ? requestedId
                : UUID.randomUUID();

        AppUser user = AppUser.builder()
                .userId(userId)
                .fullName(fullName)
                .normalizedName(normalizedName)
                .email(email)
                .normalizedEmail(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .lastLoginAt(LocalDateTime.now())
                .build();

        return mapToResponse(appUserRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID userId) {
        return mapToResponse(appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId
                )));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsers(UserRole role) {
        List<AppUser> users = role == null
                ? appUserRepository.findAllByOrderByFullNameAsc()
                : appUserRepository.findAllByRoleOrderByFullNameAsc(role);

        return users.stream().map(this::mapToResponse).toList();
    }

    private void validateExistingAccount(
            AppUser account,
            UserLoginRequestDTO request,
            UserRole requestedRole
    ) {
        if (!Boolean.TRUE.equals(account.getActive())) {
            throw new IllegalArgumentException("This user account is inactive");
        }

        if (account.getRole() != requestedRole) {
            throw new IllegalArgumentException("The selected role does not match this account");
        }

        // For external/public learners (outside the organisation): allow open self-registration,
        // no fixed corporate password required, and dynamic password update so learners are never locked out.
        if (requestedRole == UserRole.LEARNER) {
            if (account.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
                account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
            return;
        }

        // For internal enterprise staff (ADMIN, HR, EMPLOYEE): verify organization credentials
        if (account.getPasswordHash() == null || account.getPasswordHash().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            return;
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
    }

    private String normalizeDisplayName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email address is required");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponseDTO mapToResponse(AppUser user) {
        String token = jwtTokenProvider.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole() != null ? user.getRole().name() : "LEARNER"
        );

        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .token(token)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
