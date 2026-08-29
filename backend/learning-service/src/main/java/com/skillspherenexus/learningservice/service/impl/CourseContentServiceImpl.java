package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.CourseContentCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseContentResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseContentUpdateRequestDTO;
import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.entity.CourseContent;
import com.skillspherenexus.learningservice.entity.CourseModule;
import com.skillspherenexus.learningservice.enums.ContentType;
import com.skillspherenexus.learningservice.enums.CoursePricingType;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.exception.DuplicateResourceException;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.CourseContentRepository;
import com.skillspherenexus.learningservice.repository.CourseModuleRepository;
import com.skillspherenexus.learningservice.repository.CourseRepository;
import com.skillspherenexus.learningservice.service.CourseContentService;
import com.skillspherenexus.learningservice.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseContentServiceImpl
        implements CourseContentService {

    private final CourseRepository courseRepository;

    private final CourseModuleRepository courseModuleRepository;

    private final CourseContentRepository courseContentRepository;

    private final EnrollmentService enrollmentService;

    @Override
    public CourseContentResponseDTO createContent(
            UUID courseId,
            UUID moduleId,
            CourseContentCreateRequestDTO request
    ) {
        findCourseById(courseId);

        CourseModule courseModule =
                findModuleById(courseId, moduleId);

        boolean duplicateOrder =
                courseContentRepository
                        .existsByCourseModuleModuleIdAndContentOrder(
                                moduleId,
                                request.getContentOrder()
                        );

        if (duplicateOrder) {
            throw new DuplicateResourceException(
                    "Content order already exists in this module: "
                            + request.getContentOrder()
            );
        }

        validateAvailabilityDates(
                request.getAvailableFrom(),
                request.getAvailableUntil()
        );

        validateContentData(
                request.getContentType(),
                request.getContentUrl(),
                request.getTextContent()
        );

        CourseContent courseContent =
                CourseContent.builder()
                        .courseModule(courseModule)
                        .title(request.getTitle().trim())
                        .description(
                                normalizeOptionalText(
                                        request.getDescription()
                                )
                        )
                        .contentType(
                                request.getContentType()
                        )
                        .contentUrl(
                                normalizeOptionalText(
                                        request.getContentUrl()
                                )
                        )
                        .textContent(
                                normalizeOptionalText(
                                        request.getTextContent()
                                )
                        )
                        .durationMinutes(
                                request.getDurationMinutes()
                        )
                        .contentOrder(
                                request.getContentOrder()
                        )
                        .mandatory(
                                request.getMandatory()
                        )
                        .previewAvailable(
                                request.getPreviewAvailable()
                        )
                        .published(false)
                        .availableFrom(
                                request.getAvailableFrom()
                        )
                        .availableUntil(
                                request.getAvailableUntil()
                        )
                        .build();

        CourseContent savedContent =
                courseContentRepository.save(courseContent);

        return convertToResponseDTO(
                savedContent,
                true,
                false
        );
    }

    @Override
    public CourseContentResponseDTO updateContent(
            UUID courseId,
            UUID moduleId,
            UUID contentId,
            CourseContentUpdateRequestDTO request
    ) {
        findCourseById(courseId);
        findModuleById(courseId, moduleId);

        CourseContent courseContent =
                findContentById(moduleId, contentId);

        boolean duplicateOrder =
                courseContentRepository
                        .existsByCourseModuleModuleIdAndContentOrderAndContentIdNot(
                                moduleId,
                                request.getContentOrder(),
                                contentId
                        );

        if (duplicateOrder) {
            throw new DuplicateResourceException(
                    "Content order already exists in this module: "
                            + request.getContentOrder()
            );
        }

        validateAvailabilityDates(
                request.getAvailableFrom(),
                request.getAvailableUntil()
        );

        validateContentData(
                request.getContentType(),
                request.getContentUrl(),
                request.getTextContent()
        );

        courseContent.setTitle(
                request.getTitle().trim()
        );

        courseContent.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        courseContent.setContentType(
                request.getContentType()
        );

        courseContent.setContentUrl(
                normalizeOptionalText(
                        request.getContentUrl()
                )
        );

        courseContent.setTextContent(
                normalizeOptionalText(
                        request.getTextContent()
                )
        );

        courseContent.setDurationMinutes(
                request.getDurationMinutes()
        );

        courseContent.setContentOrder(
                request.getContentOrder()
        );

        if (request.getMandatory() != null) {
            courseContent.setMandatory(
                    request.getMandatory()
            );
        }

        if (request.getPreviewAvailable() != null) {
            courseContent.setPreviewAvailable(
                    request.getPreviewAvailable()
            );
        }

        courseContent.setAvailableFrom(
                request.getAvailableFrom()
        );

        courseContent.setAvailableUntil(
                request.getAvailableUntil()
        );

        CourseContent updatedContent =
                courseContentRepository.save(courseContent);

        return convertToResponseDTO(
                updatedContent,
                true,
                false
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CourseContentResponseDTO getContentById(
            UUID courseId,
            UUID moduleId,
            UUID contentId
    ) {
        findCourseById(courseId);
        findModuleById(courseId, moduleId);

        CourseContent courseContent =
                findContentById(moduleId, contentId);

        return convertToResponseDTO(
                courseContent,
                true,
                false
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseContentResponseDTO> getContentsByModule(
            UUID courseId,
            UUID moduleId
    ) {
        findCourseById(courseId);
        findModuleById(courseId, moduleId);

        List<CourseContent> contents =
                courseContentRepository
                        .findByCourseModuleModuleIdOrderByContentOrderAsc(
                                moduleId
                        );

        return contents.stream()
                .map(content ->
                        convertToResponseDTO(
                                content,
                                true,
                                false
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseContentResponseDTO>
    getPublishedContentsByModule(
            UUID courseId,
            UUID moduleId
    ) {
        Course course = findCourseById(courseId);

        CourseModule courseModule =
                findModuleById(courseId, moduleId);

        validatePublishedLearnerView(
                course,
                courseModule
        );

        List<CourseContent> contents =
                courseContentRepository
                        .findByCourseModuleModuleIdAndPublishedTrueOrderByContentOrderAsc(
                                moduleId
                        );

        return contents.stream()
                .map(content ->
                        convertToResponseDTO(
                                content,
                                false,
                                false
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseContentResponseDTO>
    getPublishedContentsByModuleForLearner(
            UUID courseId,
            UUID moduleId,
            UUID learnerId
    ) {
        Course course = findCourseById(courseId);

        CourseModule courseModule =
                findModuleById(courseId, moduleId);

        validatePublishedLearnerView(
                course,
                courseModule
        );

        boolean activeCourseAccess =
                enrollmentService.hasActiveCourseAccess(
                        learnerId,
                        courseId
                );

        List<CourseContent> contents =
                courseContentRepository
                        .findByCourseModuleModuleIdAndPublishedTrueOrderByContentOrderAsc(
                                moduleId
                        );

        return contents.stream()
                .map(content ->
                        convertToResponseDTO(
                                content,
                                false,
                                activeCourseAccess
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseContentResponseDTO
    getPublishedContentByIdForLearner(
            UUID courseId,
            UUID moduleId,
            UUID contentId,
            UUID learnerId
    ) {
        Course course = findCourseById(courseId);

        CourseModule courseModule =
                findModuleById(courseId, moduleId);

        validatePublishedLearnerView(
                course,
                courseModule
        );

        CourseContent courseContent =
                findContentById(moduleId, contentId);

        if (!Boolean.TRUE.equals(
                courseContent.getPublished()
        )) {
            throw new ResourceNotFoundException(
                    "Published content not found with ID: "
                            + contentId
            );
        }

        boolean activeCourseAccess =
                enrollmentService.hasActiveCourseAccess(
                        learnerId,
                        courseId
                );

        return convertToResponseDTO(
                courseContent,
                false,
                activeCourseAccess
        );
    }

    @Override
    public CourseContentResponseDTO publishContent(
            UUID courseId,
            UUID moduleId,
            UUID contentId
    ) {
        findCourseById(courseId);
        findModuleById(courseId, moduleId);

        CourseContent courseContent =
                findContentById(moduleId, contentId);

        validateAvailabilityDates(
                courseContent.getAvailableFrom(),
                courseContent.getAvailableUntil()
        );

        validateContentData(
                courseContent.getContentType(),
                courseContent.getContentUrl(),
                courseContent.getTextContent()
        );

        courseContent.setPublished(true);

        CourseContent publishedContent =
                courseContentRepository.save(courseContent);

        return convertToResponseDTO(
                publishedContent,
                true,
                false
        );
    }

    @Override
    public CourseContentResponseDTO unpublishContent(
            UUID courseId,
            UUID moduleId,
            UUID contentId
    ) {
        findCourseById(courseId);
        findModuleById(courseId, moduleId);

        CourseContent courseContent =
                findContentById(moduleId, contentId);

        courseContent.setPublished(false);

        CourseContent unpublishedContent =
                courseContentRepository.save(courseContent);

        return convertToResponseDTO(
                unpublishedContent,
                true,
                false
        );
    }

    @Override
    public void deleteContent(
            UUID courseId,
            UUID moduleId,
            UUID contentId
    ) {
        findCourseById(courseId);
        findModuleById(courseId, moduleId);

        CourseContent courseContent =
                findContentById(moduleId, contentId);

        if (Boolean.TRUE.equals(
                courseContent.getPublished()
        )) {
            throw new IllegalArgumentException(
                    "Published content must be unpublished "
                            + "before deletion"
            );
        }

        courseContentRepository.delete(courseContent);
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

    private CourseContent findContentById(
            UUID moduleId,
            UUID contentId
    ) {
        return courseContentRepository
                .findByContentIdAndCourseModuleModuleId(
                        contentId,
                        moduleId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Content not found with ID: "
                                        + contentId
                                        + " in module: "
                                        + moduleId
                        )
                );
    }

    private void validatePublishedLearnerView(
            Course course,
            CourseModule courseModule
    ) {
        if (course.getStatus()
                != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "Course must be published before "
                            + "learners can view its contents"
            );
        }

        if (!Boolean.TRUE.equals(
                courseModule.getPublished()
        )) {
            throw new IllegalArgumentException(
                    "Module is not published"
            );
        }
    }

    private void validateAvailabilityDates(
            LocalDateTime availableFrom,
            LocalDateTime availableUntil
    ) {
        if (availableFrom != null
                && availableUntil != null
                && availableUntil.isBefore(
                availableFrom
        )) {
            throw new IllegalArgumentException(
                    "Content available-until time cannot be "
                            + "before available-from time"
            );
        }
    }

    private void validateContentData(
            ContentType contentType,
            String contentUrl,
            String textContent
    ) {
        boolean urlRequired =
                contentType == ContentType.VIDEO
                        || contentType
                        == ContentType.DOCUMENT
                        || contentType
                        == ContentType.EXTERNAL_LINK
                        || contentType
                        == ContentType.LIVE_SESSION;

        if (urlRequired && isBlank(contentUrl)) {
            throw new IllegalArgumentException(
                    "Content URL is required for content type: "
                            + contentType
            );
        }

        if (contentType == ContentType.ARTICLE
                && isBlank(textContent)) {
            throw new IllegalArgumentException(
                    "Text content is required for ARTICLE content"
            );
        }
    }

    private boolean calculateCurrentlyAvailable(
            CourseContent courseContent
    ) {
        if (!Boolean.TRUE.equals(
                courseContent.getPublished()
        )) {
            return false;
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        if (courseContent.getAvailableFrom() != null
                && currentTime.isBefore(
                courseContent.getAvailableFrom()
        )) {
            return false;
        }

        if (courseContent.getAvailableUntil() != null
                && currentTime.isAfter(
                courseContent.getAvailableUntil()
        )) {
            return false;
        }

        return true;
    }

    private CourseContentResponseDTO convertToResponseDTO(
            CourseContent courseContent,
            boolean administratorView,
            boolean activeCourseAccess
    ) {
        CourseModule courseModule =
                courseContent.getCourseModule();

        Course course =
                courseModule.getCourse();

        boolean currentlyAvailable =
                calculateCurrentlyAvailable(
                        courseContent
                );

        boolean previewCanBeShown =
                Boolean.TRUE.equals(
                        courseContent.getPreviewAvailable()
                )
                        && currentlyAvailable;

        boolean freeCourseContentCanBeShown =
                course.getPricingType()
                        == CoursePricingType.FREE
                        && currentlyAvailable;

        boolean enrolledLearnerContentCanBeShown =
                activeCourseAccess
                        && currentlyAvailable;

        boolean exposeContent =
                administratorView
                        || previewCanBeShown
                        || freeCourseContentCanBeShown
                        || enrolledLearnerContentCanBeShown;

        return CourseContentResponseDTO.builder()
                .contentId(
                        courseContent.getContentId()
                )
                .moduleId(
                        courseModule.getModuleId()
                )
                .moduleTitle(
                        courseModule.getTitle()
                )
                .courseId(
                        course.getCourseId()
                )
                .courseCode(
                        course.getCourseCode()
                )
                .courseTitle(
                        course.getTitle()
                )
                .title(
                        courseContent.getTitle()
                )
                .description(
                        courseContent.getDescription()
                )
                .contentType(
                        courseContent.getContentType()
                )
                .contentUrl(
                        exposeContent
                                ? courseContent.getContentUrl()
                                : null
                )
                .textContent(
                        exposeContent
                                ? courseContent.getTextContent()
                                : null
                )
                .durationMinutes(
                        courseContent.getDurationMinutes()
                )
                .contentOrder(
                        courseContent.getContentOrder()
                )
                .mandatory(
                        courseContent.getMandatory()
                )
                .previewAvailable(
                        courseContent.getPreviewAvailable()
                )
                .published(
                        courseContent.getPublished()
                )
                .availableFrom(
                        courseContent.getAvailableFrom()
                )
                .availableUntil(
                        courseContent.getAvailableUntil()
                )
                .currentlyAvailable(
                        currentlyAvailable
                )
                .createdAt(
                        courseContent.getCreatedAt()
                )
                .updatedAt(
                        courseContent.getUpdatedAt()
                )
                .build();
    }

    private String normalizeOptionalText(
            String text
    ) {
        if (text == null) {
            return null;
        }

        String normalizedText =
                text.trim();

        return normalizedText.isEmpty()
                ? null
                : normalizedText;
    }

    private boolean isBlank(String text) {
        return text == null
                || text.trim().isEmpty();
    }
}