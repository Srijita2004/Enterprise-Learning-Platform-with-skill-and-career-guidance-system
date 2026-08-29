package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.CourseModuleCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseModuleResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseModuleUpdateRequestDTO;
import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.entity.CourseModule;
import com.skillspherenexus.learningservice.exception.DuplicateResourceException;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.CourseContentRepository;
import com.skillspherenexus.learningservice.repository.CourseModuleRepository;
import com.skillspherenexus.learningservice.repository.CourseRepository;
import com.skillspherenexus.learningservice.service.CourseModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.skillspherenexus.learningservice.enums.CourseStatus;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseModuleServiceImpl implements CourseModuleService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final CourseContentRepository courseContentRepository;

    @Override
    public CourseModuleResponseDTO createModule(
            UUID courseId,
            CourseModuleCreateRequestDTO request
    ) {
        Course course = findCourseById(courseId);

        if (courseModuleRepository
                .existsByCourseCourseIdAndModuleOrder(
                        courseId,
                        request.getModuleOrder()
                )) {
            throw new DuplicateResourceException(
                    "Module order already exists in this course: "
                            + request.getModuleOrder()
            );
        }

        CourseModule courseModule = CourseModule.builder()
                .course(course)
                .title(request.getTitle().trim())
                .description(
                        normalizeOptionalText(
                                request.getDescription()
                        )
                )
                .moduleOrder(request.getModuleOrder())
                .published(false)
                .build();

        CourseModule savedModule =
                courseModuleRepository.save(courseModule);

        return convertToResponseDTO(savedModule);
    }

    @Override
    public CourseModuleResponseDTO updateModule(
            UUID courseId,
            UUID moduleId,
            CourseModuleUpdateRequestDTO request
    ) {
        findCourseById(courseId);

        CourseModule courseModule =
                findModuleById(courseId, moduleId);

        boolean duplicateOrder =
                courseModuleRepository
                        .existsByCourseCourseIdAndModuleOrderAndModuleIdNot(
                                courseId,
                                request.getModuleOrder(),
                                moduleId
                        );

        if (duplicateOrder) {
            throw new DuplicateResourceException(
                    "Module order already exists in this course: "
                            + request.getModuleOrder()
            );
        }

        courseModule.setTitle(
                request.getTitle().trim()
        );

        courseModule.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        courseModule.setModuleOrder(
                request.getModuleOrder()
        );

        CourseModule updatedModule =
                courseModuleRepository.save(courseModule);

        return convertToResponseDTO(updatedModule);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseModuleResponseDTO getModuleById(
            UUID courseId,
            UUID moduleId
    ) {
        findCourseById(courseId);

        CourseModule courseModule =
                findModuleById(courseId, moduleId);

        return convertToResponseDTO(courseModule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseModuleResponseDTO> getModulesByCourse(
            UUID courseId
    ) {
        findCourseById(courseId);

        List<CourseModule> modules =
                courseModuleRepository
                        .findByCourseCourseIdOrderByModuleOrderAsc(
                                courseId
                        );

        return convertToResponseDTOList(modules);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseModuleResponseDTO>
    getPublishedModulesByCourse(UUID courseId) {

        Course course = findCourseById(courseId);

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "Course must be published before learners can view its modules"
            );
        }

        List<CourseModule> modules =
                courseModuleRepository
                        .findByCourseCourseIdAndPublishedTrueOrderByModuleOrderAsc(
                                courseId
                        );

        return convertToResponseDTOList(modules);
    }

    @Override
    public CourseModuleResponseDTO publishModule(
            UUID courseId,
            UUID moduleId
    ) {
        findCourseById(courseId);

        CourseModule courseModule =
                findModuleById(courseId, moduleId);

        long publishedContentCount =
                courseContentRepository
                        .countByCourseModuleModuleIdAndPublishedTrue(
                                moduleId
                        );

        if (publishedContentCount == 0) {
            throw new IllegalArgumentException(
                    "Module must contain at least one published content before publishing"
            );
        }

        courseModule.setPublished(true);

        CourseModule publishedModule =
                courseModuleRepository.save(courseModule);

        return convertToResponseDTO(publishedModule);
    }

    @Override
    public CourseModuleResponseDTO unpublishModule(
            UUID courseId,
            UUID moduleId
    ) {
        findCourseById(courseId);

        CourseModule courseModule =
                findModuleById(courseId, moduleId);

        courseModule.setPublished(false);

        CourseModule unpublishedModule =
                courseModuleRepository.save(courseModule);

        return convertToResponseDTO(unpublishedModule);
    }

    @Override
    public void deleteModule(
            UUID courseId,
            UUID moduleId
    ) {
        findCourseById(courseId);

        CourseModule courseModule =
                findModuleById(courseId, moduleId);

        if (Boolean.TRUE.equals(courseModule.getPublished())) {
            throw new IllegalArgumentException(
                    "Published module must be unpublished before deletion"
            );
        }

        long contentCount =
                courseContentRepository
                        .countByCourseModuleModuleId(moduleId);

        if (contentCount > 0) {
            throw new IllegalArgumentException(
                    "Module containing content cannot be deleted"
            );
        }

        courseModuleRepository.delete(courseModule);
    }

    private Course findCourseById(UUID courseId) {

        return courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found with ID: "
                                        + courseId
                        )
                );
    }

    private CourseModule findModuleById(
            UUID courseId,
            UUID moduleId
    ) {
        return courseModuleRepository
                .findByModuleIdAndCourseCourseId(
                        moduleId,
                        courseId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Module not found with ID: "
                                        + moduleId
                                        + " in course: "
                                        + courseId
                        )
                );
    }

    private String normalizeOptionalText(String text) {

        if (text == null) {
            return null;
        }

        String normalizedText = text.trim();

        return normalizedText.isEmpty()
                ? null
                : normalizedText;
    }

    private List<CourseModuleResponseDTO>
    convertToResponseDTOList(List<CourseModule> modules) {

        return modules.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    private CourseModuleResponseDTO convertToResponseDTO(
            CourseModule courseModule
    ) {
        UUID moduleId = courseModule.getModuleId();

        long contentCount =
                courseContentRepository
                        .countByCourseModuleModuleId(moduleId);

        long publishedContentCount =
                courseContentRepository
                        .countByCourseModuleModuleIdAndPublishedTrue(
                                moduleId
                        );

        Long totalDuration =
                courseContentRepository
                        .calculateTotalDurationByModuleId(
                                moduleId
                        );

        int totalDurationMinutes =
                totalDuration == null
                        ? 0
                        : Math.toIntExact(totalDuration);

        Course course = courseModule.getCourse();

        return CourseModuleResponseDTO.builder()
                .moduleId(courseModule.getModuleId())
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseTitle(course.getTitle())
                .title(courseModule.getTitle())
                .description(courseModule.getDescription())
                .moduleOrder(courseModule.getModuleOrder())
                .published(courseModule.getPublished())
                .contentCount(contentCount)
                .publishedContentCount(
                        publishedContentCount
                )
                .totalDurationMinutes(
                        totalDurationMinutes
                )
                .createdAt(courseModule.getCreatedAt())
                .updatedAt(courseModule.getUpdatedAt())
                .build();
    }
}