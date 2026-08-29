package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.CourseCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseUpdateRequestDTO;
import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.CoursePricingType;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.CourseType;
import com.skillspherenexus.learningservice.exception.DuplicateResourceException;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.CourseRepository;
import com.skillspherenexus.learningservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public CourseResponseDTO createCourse(CourseCreateRequestDTO request) {

        String normalizedCourseCode =
                normalizeCourseCode(request.getCourseCode());

        if (courseRepository.existsByCourseCodeIgnoreCase(
                normalizedCourseCode
        )) {
            throw new DuplicateResourceException(
                    "Course code already exists: "
                            + normalizedCourseCode
            );
        }

        validateDates(
                request.getStartDate(),
                request.getEndDate()
        );

        validatePricing(
                request.getPricingType(),
                request.getPrice()
        );

        Course course = Course.builder()
                .courseCode(normalizedCourseCode)
                .title(request.getTitle().trim())
                .description(
                        normalizeOptionalText(
                                request.getDescription()
                        )
                )
                .category(request.getCategory().trim())
                .courseType(request.getCourseType())
                .courseLevel(request.getCourseLevel())
                .status(CourseStatus.DRAFT)
                .pricingType(request.getPricingType())
                .price(request.getPrice())
                .currencyCode(
                        normalizeCurrencyCode(
                                request.getCurrencyCode()
                        )
                )
                .instructorName(
                        request.getInstructorName().trim()
                )
                .durationHours(request.getDurationHours())
                .maxCapacity(request.getMaxCapacity())
                .passingScore(request.getPassingScore())
                .certificateEnabled(
                        request.getCertificateEnabled()
                )
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .averageRating(0.0)
                .ratingCount(0)
                .build();

        Course savedCourse = courseRepository.save(course);

        return convertToResponseDTO(savedCourse);
    }

    @Override
    public CourseResponseDTO updateCourse(
            UUID courseId,
            CourseUpdateRequestDTO request
    ) {

        Course course = findCourseById(courseId);

        validateDates(
                request.getStartDate(),
                request.getEndDate()
        );

        validatePricing(
                request.getPricingType(),
                request.getPrice()
        );

        course.setTitle(request.getTitle().trim());

        course.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        course.setCategory(
                request.getCategory().trim()
        );

        course.setCourseType(
                request.getCourseType()
        );

        course.setCourseLevel(
                request.getCourseLevel()
        );

        course.setPricingType(
                request.getPricingType()
        );

        course.setPrice(
                request.getPrice()
        );

        course.setCurrencyCode(
                normalizeCurrencyCode(
                        request.getCurrencyCode()
                )
        );

        course.setInstructorName(
                request.getInstructorName().trim()
        );

        course.setDurationHours(
                request.getDurationHours()
        );

        course.setMaxCapacity(
                request.getMaxCapacity()
        );

        course.setPassingScore(
                request.getPassingScore()
        );

        course.setCertificateEnabled(
                request.getCertificateEnabled()
        );

        course.setStartDate(
                request.getStartDate()
        );

        course.setEndDate(
                request.getEndDate()
        );

        Course updatedCourse = courseRepository.save(course);

        return convertToResponseDTO(updatedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDTO getCourseById(UUID courseId) {

        Course course = findCourseById(courseId);

        return convertToResponseDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDTO getCourseByCode(
            String courseCode
    ) {

        String normalizedCourseCode =
                normalizeCourseCode(courseCode);

        Course course = courseRepository
                .findByCourseCodeIgnoreCase(
                        normalizedCourseCode
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found with code: "
                                        + normalizedCourseCode
                        )
                );

        return convertToResponseDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getAllCourses() {

        List<Course> courses = courseRepository.findAll(
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        return convertToResponseDTOList(courses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> searchCoursesByTitle(
            String title
    ) {

        String searchTitle = requireSearchText(
                title,
                "Course title"
        );

        List<Course> courses =
                courseRepository
                        .findByTitleContainingIgnoreCase(
                                searchTitle
                        );

        return convertToResponseDTOList(courses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getCoursesByCategory(
            String category
    ) {

        String searchCategory = requireSearchText(
                category,
                "Course category"
        );

        List<Course> courses =
                courseRepository
                        .findByCategoryIgnoreCase(
                                searchCategory
                        );

        return convertToResponseDTOList(courses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getCoursesByType(
            CourseType courseType
    ) {

        List<Course> courses =
                courseRepository.findByCourseType(courseType);

        return convertToResponseDTOList(courses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getCoursesByLevel(
            CourseLevel courseLevel
    ) {

        List<Course> courses =
                courseRepository.findByCourseLevel(
                        courseLevel
                );

        return convertToResponseDTOList(courses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getCoursesByStatus(
            CourseStatus status
    ) {

        List<Course> courses =
                courseRepository.findByStatus(status);

        return convertToResponseDTOList(courses);
    }

    @Override
    public CourseResponseDTO publishCourse(UUID courseId) {

        Course course = findCourseById(courseId);

        course.setStatus(CourseStatus.PUBLISHED);

        Course publishedCourse =
                courseRepository.save(course);

        return convertToResponseDTO(publishedCourse);
    }

    @Override
    public CourseResponseDTO archiveCourse(UUID courseId) {

        Course course = findCourseById(courseId);

        course.setStatus(CourseStatus.ARCHIVED);

        Course archivedCourse =
                courseRepository.save(course);

        return convertToResponseDTO(archivedCourse);
    }

    @Override
    public void deleteCourse(UUID courseId) {

        Course course = findCourseById(courseId);

        if (course.getStatus() == CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "Published course must be archived before deletion"
            );
        }

        courseRepository.delete(course);
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

    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }
    }

    private void validatePricing(
            CoursePricingType pricingType,
            BigDecimal price
    ) {

        if (pricingType == null || price == null) {
            throw new IllegalArgumentException(
                    "Pricing type and price are required"
            );
        }

        if (pricingType == CoursePricingType.FREE
                && price.compareTo(BigDecimal.ZERO) != 0) {

            throw new IllegalArgumentException(
                    "Free course price must be 0"
            );
        }

        if (pricingType == CoursePricingType.PAID
                && price.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Paid course price must be greater than 0"
            );
        }
    }

    private String normalizeCourseCode(
            String courseCode
    ) {

        return courseCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeCurrencyCode(
            String currencyCode
    ) {

        return currencyCode
                .trim()
                .toUpperCase(Locale.ROOT);
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

    private String requireSearchText(
            String value,
            String fieldName
    ) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty"
            );
        }

        return value.trim();
    }

    private List<CourseResponseDTO>
    convertToResponseDTOList(List<Course> courses) {

        return courses.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    private CourseResponseDTO convertToResponseDTO(
            Course course
    ) {

        long enrolledCount =
                getCurrentEnrolledCount(
                        course.getCourseId()
                );

        long completedCount =
                getCurrentCompletedCount(
                        course.getCourseId()
                );

        long remainingSeats =
                (long) course.getMaxCapacity()
                        - enrolledCount;

        int availableSeats =
                (int) Math.max(remainingSeats, 0);

        double completionRate =
                calculateCompletionRate(
                        enrolledCount,
                        completedCount
                );

        return CourseResponseDTO.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .courseType(course.getCourseType())
                .courseLevel(course.getCourseLevel())
                .status(course.getStatus())
                .pricingType(course.getPricingType())
                .price(course.getPrice())
                .currencyCode(course.getCurrencyCode())
                .instructorName(
                        course.getInstructorName()
                )
                .durationHours(
                        course.getDurationHours()
                )
                .maxCapacity(course.getMaxCapacity())
                .passingScore(course.getPassingScore())
                .certificateEnabled(
                        course.getCertificateEnabled()
                )
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .averageRating(
                        course.getAverageRating()
                )
                .ratingCount(course.getRatingCount())
                .enrolledCount(enrolledCount)
                .completedCount(completedCount)
                .availableSeats(availableSeats)
                .completionRate(completionRate)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private double calculateCompletionRate(
            long enrolledCount,
            long completedCount
    ) {

        if (enrolledCount == 0) {
            return 0.0;
        }

        double rate =
                completedCount * 100.0 / enrolledCount;

        return Math.round(rate * 100.0) / 100.0;
    }

    private long getCurrentEnrolledCount(UUID courseId) {

        return 0L;
    }

    private long getCurrentCompletedCount(UUID courseId) {

        return 0L;
    }
}
