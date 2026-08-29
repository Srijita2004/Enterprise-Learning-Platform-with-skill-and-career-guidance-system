package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.LearningPath;
import com.skillspherenexus.learningservice.enums.LearningPathStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningPathRepository
        extends JpaRepository<LearningPath, UUID> {

    Optional<LearningPath> findByPathCodeIgnoreCase(
            String pathCode
    );

    boolean existsByPathCodeIgnoreCase(
            String pathCode
    );

    boolean existsByPathCodeIgnoreCaseAndPathIdNot(
            String pathCode,
            UUID pathId
    );

    List<LearningPath> findAllByOrderByCreatedAtDesc();

    List<LearningPath> findAllByStatusOrderByCreatedAtDesc(
            LearningPathStatus status
    );

    List<LearningPath> findAllByCategoryIgnoreCaseOrderByTitleAsc(
            String category
    );

    List<LearningPath> findAllByTargetRoleIgnoreCaseOrderByTitleAsc(
            String targetRole
    );

    List<LearningPath>
    findAllByStatusAndCategoryIgnoreCaseOrderByTitleAsc(
            LearningPathStatus status,
            String category
    );

    List<LearningPath>
    findAllByStatusAndTargetRoleIgnoreCaseOrderByTitleAsc(
            LearningPathStatus status,
            String targetRole
    );

    long countByStatus(
            LearningPathStatus status
    );
}