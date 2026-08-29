package com.skillspherenexus.learningservice.config;

import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.entity.CourseContent;
import com.skillspherenexus.learningservice.entity.CourseModule;
import com.skillspherenexus.learningservice.enums.ContentType;
import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.CoursePricingType;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.CourseType;
import com.skillspherenexus.learningservice.repository.CourseContentRepository;
import com.skillspherenexus.learningservice.repository.CourseModuleRepository;
import com.skillspherenexus.learningservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LearningDataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final CourseContentRepository courseContentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (courseRepository.count() > 0) {
            log.info("Learning courses already exist ({} courses). Skipping seed.", courseRepository.count());
            return;
        }

        log.info("Seeding initial benchmark courses, modules, and learning lessons...");

        Course course1 = Course.builder()
                .courseCode("CRS-SPRING-01")
                .title("Spring Boot 4 & Cloud Microservices Architecture")
                .description("Master enterprise backend engineering with Spring Boot 4, Eureka discovery, Spring Cloud Gateway, and PostgreSQL isolation.")
                .category("Backend Engineering")
                .courseType(CourseType.ONLINE)
                .courseLevel(CourseLevel.INTERMEDIATE)
                .status(CourseStatus.PUBLISHED)
                .pricingType(CoursePricingType.FREE)
                .price(BigDecimal.ZERO)
                .currencyCode("USD")
                .instructorName("Dr. Alex Vance")
                .durationHours(24)
                .maxCapacity(100)
                .passingScore(75)
                .certificateEnabled(true)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusMonths(3))
                .averageRating(4.9)
                .ratingCount(34)
                .build();
        Course savedCourse1 = courseRepository.save(course1);

        CourseModule module1_1 = CourseModule.builder()
                .course(savedCourse1)
                .title("Module 1: Microservice Foundations & API Gateway")
                .description("Service discovery, declarative routing, and JWT stateless header propagation.")
                .moduleOrder(1)
                .published(true)
                .build();
        CourseModule savedMod1_1 = courseModuleRepository.save(module1_1);

        CourseContent content1_1_1 = CourseContent.builder()
                .courseModule(savedMod1_1)
                .title("Lesson 1: Service Discovery with Netflix Eureka")
                .description("Learn how Eureka server registers and monitors distributed Spring Boot instances.")
                .contentType(ContentType.VIDEO)
                .contentUrl("https://www.youtube.com/embed/dQw4w9WgXcQ")
                .durationMinutes(35)
                .contentOrder(1)
                .mandatory(true)
                .previewAvailable(true)
                .published(true)
                .build();
        courseContentRepository.save(content1_1_1);

        CourseContent content1_1_2 = CourseContent.builder()
                .courseModule(savedMod1_1)
                .title("Lesson 2: API Gateway Routing & RBAC Filters")
                .description("Deep dive into Global Filters, Rate Limiting, and X-User-Role header injection.")
                .contentType(ContentType.ARTICLE)
                .textContent("Spring Cloud Gateway provides declarative route definitions matching path predicates and applying filters before forwarding requests downstream.")
                .durationMinutes(20)
                .contentOrder(2)
                .mandatory(true)
                .previewAvailable(false)
                .published(true)
                .build();
        courseContentRepository.save(content1_1_2);

        Course course2 = Course.builder()
                .courseCode("CRS-ANGULAR-01")
                .title("Modern Angular 21 Enterprise Frontend Architecture")
                .description("Learn Signals, Standalone Components, Reactive Forms, and Role-Guarded UI patterns in Angular 21.")
                .category("Frontend Engineering")
                .courseType(CourseType.ONLINE)
                .courseLevel(CourseLevel.ADVANCED)
                .status(CourseStatus.PUBLISHED)
                .pricingType(CoursePricingType.FREE)
                .price(BigDecimal.ZERO)
                .currencyCode("USD")
                .instructorName("Sarah Jenkins")
                .durationHours(18)
                .maxCapacity(80)
                .passingScore(70)
                .certificateEnabled(true)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusMonths(2))
                .averageRating(4.8)
                .ratingCount(22)
                .build();
        Course savedCourse2 = courseRepository.save(course2);

        CourseModule module2_1 = CourseModule.builder()
                .course(savedCourse2)
                .title("Module 1: Signals & Reactive State")
                .description("Replacing RxJS subjects with Angular Signals for fine-grained reactivity.")
                .moduleOrder(1)
                .published(true)
                .build();
        CourseModule savedMod2_1 = courseModuleRepository.save(module2_1);

        CourseContent content2_1_1 = CourseContent.builder()
                .courseModule(savedMod2_1)
                .title("Lesson 1: Signal Primitives (signal, computed, effect)")
                .description("Master signal state management without zone.js overhead.")
                .contentType(ContentType.DOCUMENT)
                .textContent("Angular Signals track dependencies automatically during template execution and notify the renderer when values change.")
                .durationMinutes(25)
                .contentOrder(1)
                .mandatory(true)
                .previewAvailable(true)
                .published(true)
                .build();
        courseContentRepository.save(content2_1_1);

        Course course3 = Course.builder()
                .courseCode("CRS-DEVOPS-01")
                .title("Enterprise CI/CD, Docker & Container Orchestration")
                .description("Docker multi-stage builds, Nginx reverse proxying, docker-compose orchestration, and production readiness.")
                .category("DevOps & Cloud")
                .courseType(CourseType.WORKSHOP)
                .courseLevel(CourseLevel.INTERMEDIATE)
                .status(CourseStatus.PUBLISHED)
                .pricingType(CoursePricingType.FREE)
                .price(BigDecimal.ZERO)
                .currencyCode("USD")
                .instructorName("Michael Zhang")
                .durationHours(15)
                .maxCapacity(60)
                .passingScore(80)
                .certificateEnabled(true)
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusMonths(1))
                .averageRating(4.9)
                .ratingCount(18)
                .build();
        courseRepository.save(course3);

        log.info("Successfully seeded 3 benchmark learning courses with modules and lessons.");
    }
}
