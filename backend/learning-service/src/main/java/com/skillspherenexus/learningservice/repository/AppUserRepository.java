package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.AppUser;
import com.skillspherenexus.learningservice.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByNormalizedNameAndRole(
            String normalizedName,
            UserRole role
    );

    Optional<AppUser> findByNormalizedEmail(String normalizedEmail);

    boolean existsByNormalizedEmail(String normalizedEmail);

    List<AppUser> findAllByRoleOrderByFullNameAsc(UserRole role);

    List<AppUser> findAllByOrderByFullNameAsc();

    long countByRole(UserRole role);
}
