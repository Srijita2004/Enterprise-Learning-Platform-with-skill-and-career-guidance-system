package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.CertificateIssueRequestDTO;
import com.skillspherenexus.learningservice.dto.CertificateResponseDTO;
import com.skillspherenexus.learningservice.dto.CertificateRevocationRequestDTO;
import com.skillspherenexus.learningservice.entity.Certificate;
import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.entity.CourseCompletion;
import com.skillspherenexus.learningservice.entity.Enrollment;
import com.skillspherenexus.learningservice.enums.CertificateStatus;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.CertificateRepository;
import com.skillspherenexus.learningservice.repository.CourseCompletionRepository;
import com.skillspherenexus.learningservice.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificateServiceImpl
        implements CertificateService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy",
                    Locale.ENGLISH
            );

    /*
     * PDFBox 3 RGB colours use float values from 0 to 1.
     */

    private static final float[] NAVY =
            {0.035f, 0.082f, 0.165f};

    private static final float[] ROYAL_BLUE =
            {0.055f, 0.220f, 0.455f};

    private static final float[] BRIGHT_BLUE =
            {0.090f, 0.380f, 0.690f};

    private static final float[] GOLD =
            {0.830f, 0.640f, 0.210f};

    private static final float[] LIGHT_GOLD =
            {0.960f, 0.870f, 0.550f};

    private static final float[] CREAM =
            {0.985f, 0.970f, 0.925f};

    private static final float[] WHITE =
            {1.000f, 1.000f, 1.000f};

    private static final float[] DARK_TEXT =
            {0.075f, 0.090f, 0.120f};

    private static final float[] MUTED_TEXT =
            {0.310f, 0.340f, 0.390f};

    private static final float[] SOFT_BLUE =
            {0.920f, 0.950f, 0.985f};

    private static final float[] GREEN =
            {0.090f, 0.520f, 0.300f};

    private static final float[] RED =
            {0.750f, 0.100f, 0.120f};

    private final CertificateRepository certificateRepository;

    private final CourseCompletionRepository
            courseCompletionRepository;

    @Override
    public CertificateResponseDTO issueCertificate(
            UUID completionId,
            CertificateIssueRequestDTO request
    ) {
        Certificate existingCertificate =
                certificateRepository
                        .findByCourseCompletionCompletionId(
                                completionId
                        )
                        .orElse(null);

        if (existingCertificate != null) {
            return mapToResponse(existingCertificate);
        }

        CourseCompletion completion =
                getCourseCompletionOrThrow(completionId);

        validateCertificateEligibility(completion);

        Enrollment enrollment =
                completion.getEnrollment();

        Course course =
                enrollment.getCourse();

        Certificate certificate =
                Certificate.builder()
                        .courseCompletion(completion)
                        .learnerId(
                                completion.getLearnerId()
                        )
                        .courseId(
                                completion.getCourseId()
                        )
                        .courseCode(
                                course.getCourseCode()
                        )
                        .courseTitle(
                                course.getTitle()
                        )
                        .recipientName(
                                request.getRecipientName().trim()
                        )
                        .certificateNumber(
                                generateUniqueCertificateNumber()
                        )
                        .verificationCode(
                                generateUniqueVerificationCode()
                        )
                        .status(
                                CertificateStatus.ISSUED
                        )
                        .issuedByUserId(
                                request.getIssuedByUserId()
                        )
                        .fileUrl(
                                normalizeOptionalText(
                                        request.getFileUrl()
                                )
                        )
                        .courseCompletedAt(
                                completion.getCompletedAt()
                        )
                        .issuedAt(
                                LocalDateTime.now()
                        )
                        .build();

        Certificate savedCertificate =
                certificateRepository.save(certificate);

        if (savedCertificate.getFileUrl() == null) {
            savedCertificate.setFileUrl(
                    "/api/certificates/"
                            + savedCertificate.getCertificateId()
                            + "/pdf"
            );

            savedCertificate =
                    certificateRepository.save(
                            savedCertificate
                    );
        }

        return mapToResponse(savedCertificate);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponseDTO getCertificateById(
            UUID certificateId
    ) {
        return mapToResponse(
                getCertificateOrThrow(certificateId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponseDTO getCertificateByCompletion(
            UUID completionId
    ) {
        Certificate certificate =
                certificateRepository
                        .findByCourseCompletionCompletionId(
                                completionId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Certificate not found for "
                                                + "course completion: "
                                                + completionId
                                )
                        );

        return mapToResponse(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponseDTO getCertificateByNumber(
            String certificateNumber
    ) {
        if (certificateNumber == null
                || certificateNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Certificate number is required"
            );
        }

        String normalizedNumber =
                certificateNumber
                        .trim()
                        .toUpperCase(Locale.ROOT);

        Certificate certificate =
                certificateRepository
                        .findByCertificateNumberIgnoreCase(
                                normalizedNumber
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Certificate not found with number: "
                                                + normalizedNumber
                                )
                        );

        return mapToResponse(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponseDTO verifyCertificate(
            UUID verificationCode
    ) {
        Certificate certificate =
                certificateRepository
                        .findByVerificationCode(
                                verificationCode
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Certificate not found with "
                                                + "verification code: "
                                                + verificationCode
                                )
                        );

        return mapToResponse(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponseDTO>
    getCertificatesByLearner(
            UUID learnerId
    ) {
        return certificateRepository
                .findAllByLearnerIdOrderByIssuedAtDesc(
                        learnerId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponseDTO>
    getCertificatesByCourse(
            UUID courseId
    ) {
        return certificateRepository
                .findAllByCourseIdOrderByIssuedAtDesc(
                        courseId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponseDTO> getAllCertificates() {
        return certificateRepository
                .findAllByOrderByIssuedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponseDTO>
    getCertificatesByStatus(
            CertificateStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Certificate status is required"
            );
        }

        return certificateRepository
                .findAllByStatusOrderByIssuedAtDesc(
                        status
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CertificateResponseDTO revokeCertificate(
            UUID certificateId,
            CertificateRevocationRequestDTO request
    ) {
        Certificate certificate =
                getCertificateOrThrow(certificateId);

        if (certificate.getStatus()
                == CertificateStatus.REVOKED) {
            return mapToResponse(certificate);
        }

        certificate.setStatus(
                CertificateStatus.REVOKED
        );

        certificate.setRevokedByUserId(
                request.getRevokedByUserId()
        );

        certificate.setRevokedAt(
                LocalDateTime.now()
        );

        certificate.setRevocationReason(
                request.getRevocationReason().trim()
        );

        Certificate revokedCertificate =
                certificateRepository.save(certificate);

        return mapToResponse(revokedCertificate);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateCertificatePdf(
            UUID certificateId
    ) {
        Certificate certificate =
                getCertificateOrThrow(certificateId);

        PDRectangle landscapePage =
                new PDRectangle(
                        PDRectangle.A4.getHeight(),
                        PDRectangle.A4.getWidth()
                );

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            PDPage page =
                    new PDPage(landscapePage);

            document.addPage(page);

            PDFont extraBoldFont =
                    new PDType1Font(
                            Standard14Fonts.FontName
                                    .HELVETICA_BOLD
                    );

            PDFont regularFont =
                    new PDType1Font(
                            Standard14Fonts.FontName
                                    .HELVETICA
                    );

            PDFont italicFont =
                    new PDType1Font(
                            Standard14Fonts.FontName
                                    .HELVETICA_OBLIQUE
                    );

            PDFont serifBoldFont =
                    new PDType1Font(
                            Standard14Fonts.FontName
                                    .TIMES_BOLD
                    );

            float pageWidth =
                    landscapePage.getWidth();

            float pageHeight =
                    landscapePage.getHeight();

            try (
                    PDPageContentStream contentStream =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {
                drawCertificateBackground(
                        contentStream,
                        pageWidth,
                        pageHeight
                );

                drawCertificateHeader(
                        contentStream,
                        extraBoldFont,
                        regularFont,
                        pageWidth,
                        pageHeight
                );

                drawStatusBadge(
                        contentStream,
                        extraBoldFont,
                        certificate.getStatus(),
                        pageWidth,
                        pageHeight
                );

                drawMainCertificateContent(
                        contentStream,
                        extraBoldFont,
                        regularFont,
                        italicFont,
                        serifBoldFont,
                        certificate,
                        pageWidth,
                        pageHeight
                );

                drawVerificationPanel(
                        contentStream,
                        extraBoldFont,
                        regularFont,
                        certificate,
                        pageWidth
                );

                drawOfficialSeal(
                        contentStream,
                        extraBoldFont,
                        regularFont
                );

                drawSignatureArea(
                        contentStream,
                        extraBoldFont,
                        italicFont,
                        pageWidth
                );

                drawFooter(
                        contentStream,
                        regularFont,
                        italicFont,
                        certificate,
                        pageWidth
                );

                if (certificate.getStatus()
                        == CertificateStatus.REVOKED) {
                    drawRevokedBanner(
                            contentStream,
                            extraBoldFont,
                            pageWidth,
                            pageHeight
                    );
                }
            }

            document.save(outputStream);

            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to generate certificate PDF",
                    exception
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCertificateValid(
            UUID verificationCode
    ) {
        return certificateRepository
                .findByVerificationCode(
                        verificationCode
                )
                .map(certificate ->
                        certificate.getStatus()
                                == CertificateStatus.ISSUED
                )
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCertificatesByLearner(
            UUID learnerId
    ) {
        return certificateRepository
                .countByLearnerId(learnerId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCertificatesByCourse(
            UUID courseId
    ) {
        return certificateRepository
                .countByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCertificatesByCourseAndStatus(
            UUID courseId,
            CertificateStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Certificate status is required"
            );
        }

        return certificateRepository
                .countByCourseIdAndStatus(
                        courseId,
                        status
                );
    }

    /*
     * =====================================================
     * Certificate Design
     * =====================================================
     */

    private void drawCertificateBackground(
            PDPageContentStream contentStream,
            float pageWidth,
            float pageHeight
    ) throws IOException {

        drawFilledRectangle(
                contentStream,
                0,
                0,
                pageWidth,
                pageHeight,
                CREAM
        );

        drawFilledRectangle(
                contentStream,
                0,
                pageHeight - 78,
                pageWidth,
                78,
                NAVY
        );

        drawFilledRectangle(
                contentStream,
                0,
                pageHeight - 84,
                pageWidth,
                6,
                GOLD
        );

        drawFilledRectangle(
                contentStream,
                31,
                31,
                pageWidth - 62,
                pageHeight - 125,
                WHITE
        );

        drawStrokedRectangle(
                contentStream,
                23,
                23,
                pageWidth - 46,
                pageHeight - 109,
                4f,
                NAVY
        );

        drawStrokedRectangle(
                contentStream,
                31,
                31,
                pageWidth - 62,
                pageHeight - 125,
                2f,
                GOLD
        );

        drawStrokedRectangle(
                contentStream,
                38,
                38,
                pageWidth - 76,
                pageHeight - 139,
                0.8f,
                ROYAL_BLUE
        );

        drawCornerDecorations(
                contentStream,
                pageWidth,
                pageHeight
        );
    }

    private void drawCertificateHeader(
            PDPageContentStream contentStream,
            PDFont boldFont,
            PDFont regularFont,
            float pageWidth,
            float pageHeight
    ) throws IOException {

        drawCenteredText(
                contentStream,
                boldFont,
                "SKILLSPHERE NEXUS",
                25,
                pageWidth,
                pageHeight - 37,
                pageWidth - 250,
                WHITE
        );

        drawCenteredText(
                contentStream,
                regularFont,
                "LEARNING  |  GROWTH  |  EXCELLENCE",
                8.5f,
                pageWidth,
                pageHeight - 56,
                pageWidth - 300,
                LIGHT_GOLD
        );

        drawCenteredText(
                contentStream,
                boldFont,
                "CERTIFICATE OF COMPLETION",
                29,
                pageWidth,
                pageHeight - 125,
                pageWidth - 190,
                NAVY
        );

        drawLine(
                contentStream,
                pageWidth / 2f - 130,
                pageHeight - 141,
                pageWidth / 2f + 130,
                pageHeight - 141,
                2.2f,
                GOLD
        );

        drawLine(
                contentStream,
                pageWidth / 2f - 72,
                pageHeight - 147,
                pageWidth / 2f + 72,
                pageHeight - 147,
                0.8f,
                ROYAL_BLUE
        );
    }

    private void drawStatusBadge(
            PDPageContentStream contentStream,
            PDFont boldFont,
            CertificateStatus status,
            float pageWidth,
            float pageHeight
    ) throws IOException {

        float[] badgeColor =
                status == CertificateStatus.ISSUED
                        ? GREEN
                        : RED;

        String badgeText =
                status == CertificateStatus.ISSUED
                        ? "VERIFIED"
                        : "REVOKED";

        float badgeWidth = 88f;
        float badgeHeight = 23f;
        float badgeX = pageWidth - 140f;
        float badgeY = pageHeight - 122f;

        drawFilledRectangle(
                contentStream,
                badgeX,
                badgeY,
                badgeWidth,
                badgeHeight,
                badgeColor
        );

        drawCenteredTextInsideArea(
                contentStream,
                boldFont,
                badgeText,
                9.5f,
                badgeX,
                badgeY + 7f,
                badgeWidth,
                WHITE
        );
    }

    private void drawMainCertificateContent(
            PDPageContentStream contentStream,
            PDFont boldFont,
            PDFont regularFont,
            PDFont italicFont,
            PDFont serifBoldFont,
            Certificate certificate,
            float pageWidth,
            float pageHeight
    ) throws IOException {

        drawCenteredText(
                contentStream,
                italicFont,
                "This certificate is proudly presented to",
                14,
                pageWidth,
                pageHeight - 180,
                pageWidth - 220,
                MUTED_TEXT
        );

        drawCenteredText(
                contentStream,
                serifBoldFont,
                certificate.getRecipientName(),
                33,
                pageWidth,
                pageHeight - 224,
                pageWidth - 220,
                ROYAL_BLUE
        );

        float recipientLineWidth =
                calculateDecorativeLineWidth(
                        serifBoldFont,
                        sanitizePdfText(
                                certificate.getRecipientName()
                        ),
                        33
                );

        drawLine(
                contentStream,
                pageWidth / 2f
                        - recipientLineWidth / 2f,
                pageHeight - 235,
                pageWidth / 2f
                        + recipientLineWidth / 2f,
                pageHeight - 235,
                1.7f,
                GOLD
        );

        drawCenteredText(
                contentStream,
                regularFont,
                "for successfully completing all learning requirements of",
                13.5f,
                pageWidth,
                pageHeight - 270,
                pageWidth - 200,
                DARK_TEXT
        );

        drawCenteredText(
                contentStream,
                boldFont,
                certificate.getCourseTitle(),
                23,
                pageWidth,
                pageHeight - 310,
                pageWidth - 225,
                NAVY
        );

        drawCenteredText(
                contentStream,
                regularFont,
                "COURSE CODE  :  "
                        + certificate.getCourseCode(),
                10.5f,
                pageWidth,
                pageHeight - 340,
                pageWidth - 260,
                ROYAL_BLUE
        );

        String completionDate =
                formatDate(
                        certificate.getCourseCompletedAt()
                );

        String issueDate =
                formatDate(
                        certificate.getIssuedAt()
                );

        drawInformationPill(
                contentStream,
                boldFont,
                regularFont,
                "COMPLETED",
                completionDate,
                pageWidth / 2f - 205,
                pageHeight - 397
        );

        drawInformationPill(
                contentStream,
                boldFont,
                regularFont,
                "ISSUED",
                issueDate,
                pageWidth / 2f + 25,
                pageHeight - 397
        );
    }

    private void drawInformationPill(
            PDPageContentStream contentStream,
            PDFont boldFont,
            PDFont regularFont,
            String label,
            String value,
            float x,
            float y
    ) throws IOException {

        float width = 180f;
        float height = 43f;

        drawFilledRectangle(
                contentStream,
                x,
                y,
                width,
                height,
                SOFT_BLUE
        );

        drawStrokedRectangle(
                contentStream,
                x,
                y,
                width,
                height,
                0.8f,
                BRIGHT_BLUE
        );

        drawCenteredTextInsideArea(
                contentStream,
                boldFont,
                label,
                8.5f,
                x,
                y + 27f,
                width,
                ROYAL_BLUE
        );

        drawCenteredTextInsideArea(
                contentStream,
                regularFont,
                value,
                10.5f,
                x,
                y + 10f,
                width,
                DARK_TEXT
        );
    }

    private void drawVerificationPanel(
            PDPageContentStream contentStream,
            PDFont boldFont,
            PDFont regularFont,
            Certificate certificate,
            float pageWidth
    ) throws IOException {

        float panelX = pageWidth / 2f - 205f;
        float panelY = 91f;
        float panelWidth = 410f;
        float panelHeight = 82f;

        drawFilledRectangle(
                contentStream,
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                SOFT_BLUE
        );

        drawFilledRectangle(
                contentStream,
                panelX,
                panelY + panelHeight - 6f,
                panelWidth,
                6f,
                GOLD
        );

        drawStrokedRectangle(
                contentStream,
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                1.1f,
                ROYAL_BLUE
        );

        drawCenteredTextInsideArea(
                contentStream,
                boldFont,
                "DIGITAL CERTIFICATE VERIFICATION",
                9.5f,
                panelX,
                panelY + 57f,
                panelWidth,
                NAVY
        );

        drawCenteredTextInsideArea(
                contentStream,
                regularFont,
                "Certificate No: "
                        + certificate.getCertificateNumber(),
                10.5f,
                panelX,
                panelY + 36f,
                panelWidth,
                DARK_TEXT
        );

        drawCenteredTextInsideArea(
                contentStream,
                regularFont,
                "Verification Code: "
                        + certificate.getVerificationCode(),
                8.4f,
                panelX,
                panelY + 17f,
                panelWidth,
                MUTED_TEXT
        );
    }

    private void drawOfficialSeal(
            PDPageContentStream contentStream,
            PDFont boldFont,
            PDFont regularFont
    ) throws IOException {

        float centerX = 120f;
        float centerY = 132f;
        float outerRadius = 48f;
        float innerRadius = 39f;

        drawFilledCircle(
                contentStream,
                centerX,
                centerY,
                outerRadius,
                GOLD
        );

        drawFilledCircle(
                contentStream,
                centerX,
                centerY,
                innerRadius,
                NAVY
        );

        drawStrokedCircle(
                contentStream,
                centerX,
                centerY,
                32f,
                1.2f,
                LIGHT_GOLD
        );

        drawCenteredTextInsideArea(
                contentStream,
                boldFont,
                "SN",
                22,
                centerX - 35,
                centerY + 5,
                70,
                WHITE
        );

        drawCenteredTextInsideArea(
                contentStream,
                boldFont,
                "CERTIFIED",
                7.2f,
                centerX - 35,
                centerY - 14,
                70,
                LIGHT_GOLD
        );

        drawCenteredTextInsideArea(
                contentStream,
                regularFont,
                "2026",
                6.8f,
                centerX - 35,
                centerY - 26,
                70,
                WHITE
        );

        drawSealRibbons(
                contentStream,
                centerX,
                centerY,
                outerRadius
        );
    }

    private void drawSealRibbons(
            PDPageContentStream contentStream,
            float centerX,
            float centerY,
            float outerRadius
    ) throws IOException {

        setFillColor(contentStream, ROYAL_BLUE);

        contentStream.moveTo(
                centerX - 28,
                centerY - outerRadius + 8
        );

        contentStream.lineTo(
                centerX - 8,
                centerY - outerRadius + 5
        );

        contentStream.lineTo(
                centerX - 20,
                centerY - outerRadius - 35
        );

        contentStream.lineTo(
                centerX - 34,
                centerY - outerRadius - 20
        );

        contentStream.closePath();
        contentStream.fill();

        setFillColor(contentStream, BRIGHT_BLUE);

        contentStream.moveTo(
                centerX + 8,
                centerY - outerRadius + 5
        );

        contentStream.lineTo(
                centerX + 28,
                centerY - outerRadius + 8
        );

        contentStream.lineTo(
                centerX + 34,
                centerY - outerRadius - 20
        );

        contentStream.lineTo(
                centerX + 20,
                centerY - outerRadius - 35
        );

        contentStream.closePath();
        contentStream.fill();
    }

    private void drawSignatureArea(
            PDPageContentStream contentStream,
            PDFont boldFont,
            PDFont italicFont,
            float pageWidth
    ) throws IOException {

        float signatureStartX =
                pageWidth - 225f;

        float signatureEndX =
                pageWidth - 75f;

        drawCenteredTextInsideArea(
                contentStream,
                italicFont,
                "SkillSphere Nexus",
                13.5f,
                signatureStartX,
                143f,
                signatureEndX - signatureStartX,
                ROYAL_BLUE
        );

        drawLine(
                contentStream,
                signatureStartX,
                130f,
                signatureEndX,
                130f,
                1.1f,
                NAVY
        );

        drawCenteredTextInsideArea(
                contentStream,
                boldFont,
                "AUTHORIZED ISSUER",
                7.8f,
                signatureStartX,
                114f,
                signatureEndX - signatureStartX,
                MUTED_TEXT
        );

        drawCenteredTextInsideArea(
                contentStream,
                italicFont,
                "Digitally signed",
                7.5f,
                signatureStartX,
                101f,
                signatureEndX - signatureStartX,
                MUTED_TEXT
        );
    }

    private void drawFooter(
            PDPageContentStream contentStream,
            PDFont regularFont,
            PDFont italicFont,
            Certificate certificate,
            float pageWidth
    ) throws IOException {

        drawLine(
                contentStream,
                215f,
                66f,
                pageWidth - 215f,
                66f,
                0.6f,
                GOLD
        );

        String footerText =
                certificate.getStatus()
                        == CertificateStatus.ISSUED
                        ? "Authenticity can be confirmed using "
                          + "the certificate verification code."
                        : "This certificate has been revoked "
                          + "and is no longer valid.";

        drawCenteredText(
                contentStream,
                italicFont,
                footerText,
                8.2f,
                pageWidth,
                51f,
                pageWidth - 300f,
                certificate.getStatus()
                        == CertificateStatus.ISSUED
                        ? MUTED_TEXT
                        : RED
        );

        drawCenteredText(
                contentStream,
                regularFont,
                "SkillSphere Nexus  |  Enterprise Learning Platform",
                7.2f,
                pageWidth,
                39f,
                pageWidth - 300f,
                MUTED_TEXT
        );
    }

    private void drawRevokedBanner(
            PDPageContentStream contentStream,
            PDFont boldFont,
            float pageWidth,
            float pageHeight
    ) throws IOException {

        float bannerWidth = 315f;
        float bannerHeight = 44f;
        float bannerX =
                (pageWidth - bannerWidth) / 2f;
        float bannerY =
                pageHeight / 2f - 20f;

        drawFilledRectangle(
                contentStream,
                bannerX,
                bannerY,
                bannerWidth,
                bannerHeight,
                RED
        );

        drawStrokedRectangle(
                contentStream,
                bannerX - 4,
                bannerY - 4,
                bannerWidth + 8,
                bannerHeight + 8,
                2f,
                RED
        );

        drawCenteredTextInsideArea(
                contentStream,
                boldFont,
                "CERTIFICATE REVOKED",
                21,
                bannerX,
                bannerY + 13f,
                bannerWidth,
                WHITE
        );
    }

    private void drawCornerDecorations(
            PDPageContentStream contentStream,
            float pageWidth,
            float pageHeight
    ) throws IOException {

        drawTopLeftCorner(
                contentStream,
                pageHeight
        );

        drawTopRightCorner(
                contentStream,
                pageWidth,
                pageHeight
        );

        drawBottomLeftCorner(
                contentStream
        );

        drawBottomRightCorner(
                contentStream,
                pageWidth
        );
    }

    private void drawTopLeftCorner(
            PDPageContentStream contentStream,
            float pageHeight
    ) throws IOException {

        setFillColor(contentStream, GOLD);

        contentStream.moveTo(31, pageHeight - 84);
        contentStream.lineTo(105, pageHeight - 84);
        contentStream.lineTo(31, pageHeight - 158);
        contentStream.closePath();
        contentStream.fill();

        setFillColor(contentStream, ROYAL_BLUE);

        contentStream.moveTo(38, pageHeight - 91);
        contentStream.lineTo(76, pageHeight - 91);
        contentStream.lineTo(38, pageHeight - 129);
        contentStream.closePath();
        contentStream.fill();
    }

    private void drawTopRightCorner(
            PDPageContentStream contentStream,
            float pageWidth,
            float pageHeight
    ) throws IOException {

        setFillColor(contentStream, GOLD);

        contentStream.moveTo(
                pageWidth - 31,
                pageHeight - 84
        );

        contentStream.lineTo(
                pageWidth - 105,
                pageHeight - 84
        );

        contentStream.lineTo(
                pageWidth - 31,
                pageHeight - 158
        );

        contentStream.closePath();
        contentStream.fill();

        setFillColor(contentStream, ROYAL_BLUE);

        contentStream.moveTo(
                pageWidth - 38,
                pageHeight - 91
        );

        contentStream.lineTo(
                pageWidth - 76,
                pageHeight - 91
        );

        contentStream.lineTo(
                pageWidth - 38,
                pageHeight - 129
        );

        contentStream.closePath();
        contentStream.fill();
    }

    private void drawBottomLeftCorner(
            PDPageContentStream contentStream
    ) throws IOException {

        setFillColor(contentStream, GOLD);

        contentStream.moveTo(31, 31);
        contentStream.lineTo(105, 31);
        contentStream.lineTo(31, 105);
        contentStream.closePath();
        contentStream.fill();

        setFillColor(contentStream, ROYAL_BLUE);

        contentStream.moveTo(38, 38);
        contentStream.lineTo(76, 38);
        contentStream.lineTo(38, 76);
        contentStream.closePath();
        contentStream.fill();
    }

    private void drawBottomRightCorner(
            PDPageContentStream contentStream,
            float pageWidth
    ) throws IOException {

        setFillColor(contentStream, GOLD);

        contentStream.moveTo(pageWidth - 31, 31);
        contentStream.lineTo(pageWidth - 105, 31);
        contentStream.lineTo(pageWidth - 31, 105);
        contentStream.closePath();
        contentStream.fill();

        setFillColor(contentStream, ROYAL_BLUE);

        contentStream.moveTo(pageWidth - 38, 38);
        contentStream.lineTo(pageWidth - 76, 38);
        contentStream.lineTo(pageWidth - 38, 76);
        contentStream.closePath();
        contentStream.fill();
    }

    /*
     * =====================================================
     * PDF Drawing Helpers
     * =====================================================
     */

    private void drawFilledRectangle(
            PDPageContentStream contentStream,
            float x,
            float y,
            float width,
            float height,
            float[] color
    ) throws IOException {

        setFillColor(contentStream, color);

        contentStream.addRect(
                x,
                y,
                width,
                height
        );

        contentStream.fill();
    }

    private void drawStrokedRectangle(
            PDPageContentStream contentStream,
            float x,
            float y,
            float width,
            float height,
            float lineWidth,
            float[] color
    ) throws IOException {

        setStrokeColor(contentStream, color);

        contentStream.setLineWidth(lineWidth);

        contentStream.addRect(
                x,
                y,
                width,
                height
        );

        contentStream.stroke();
    }

    private void drawLine(
            PDPageContentStream contentStream,
            float startX,
            float startY,
            float endX,
            float endY,
            float lineWidth,
            float[] color
    ) throws IOException {

        setStrokeColor(contentStream, color);

        contentStream.setLineWidth(lineWidth);

        contentStream.moveTo(
                startX,
                startY
        );

        contentStream.lineTo(
                endX,
                endY
        );

        contentStream.stroke();
    }

    private void drawFilledCircle(
            PDPageContentStream contentStream,
            float centerX,
            float centerY,
            float radius,
            float[] color
    ) throws IOException {

        setFillColor(contentStream, color);

        addCirclePath(
                contentStream,
                centerX,
                centerY,
                radius
        );

        contentStream.fill();
    }

    private void drawStrokedCircle(
            PDPageContentStream contentStream,
            float centerX,
            float centerY,
            float radius,
            float lineWidth,
            float[] color
    ) throws IOException {

        setStrokeColor(contentStream, color);

        contentStream.setLineWidth(lineWidth);

        addCirclePath(
                contentStream,
                centerX,
                centerY,
                radius
        );

        contentStream.stroke();
    }

    private void addCirclePath(
            PDPageContentStream contentStream,
            float centerX,
            float centerY,
            float radius
    ) throws IOException {

        float controlPoint =
                radius * 0.55228475f;

        contentStream.moveTo(
                centerX + radius,
                centerY
        );

        contentStream.curveTo(
                centerX + radius,
                centerY + controlPoint,
                centerX + controlPoint,
                centerY + radius,
                centerX,
                centerY + radius
        );

        contentStream.curveTo(
                centerX - controlPoint,
                centerY + radius,
                centerX - radius,
                centerY + controlPoint,
                centerX - radius,
                centerY
        );

        contentStream.curveTo(
                centerX - radius,
                centerY - controlPoint,
                centerX - controlPoint,
                centerY - radius,
                centerX,
                centerY - radius
        );

        contentStream.curveTo(
                centerX + controlPoint,
                centerY - radius,
                centerX + radius,
                centerY - controlPoint,
                centerX + radius,
                centerY
        );

        contentStream.closePath();
    }

    private void drawCenteredText(
            PDPageContentStream contentStream,
            PDFont font,
            String text,
            float preferredFontSize,
            float pageWidth,
            float yPosition,
            float maximumWidth,
            float[] color
    ) throws IOException {

        String safeText =
                sanitizePdfText(text);

        float fittedFontSize =
                calculateFittedFontSize(
                        font,
                        safeText,
                        preferredFontSize,
                        maximumWidth
                );

        float textWidth =
                calculateTextWidth(
                        font,
                        safeText,
                        fittedFontSize
                );

        float xPosition =
                (pageWidth - textWidth) / 2f;

        setFillColor(contentStream, color);

        contentStream.beginText();

        contentStream.setFont(
                font,
                fittedFontSize
        );

        contentStream.newLineAtOffset(
                xPosition,
                yPosition
        );

        contentStream.showText(safeText);

        contentStream.endText();
    }

    private void drawCenteredTextInsideArea(
            PDPageContentStream contentStream,
            PDFont font,
            String text,
            float preferredFontSize,
            float areaX,
            float yPosition,
            float areaWidth,
            float[] color
    ) throws IOException {

        String safeText =
                sanitizePdfText(text);

        float fittedFontSize =
                calculateFittedFontSize(
                        font,
                        safeText,
                        preferredFontSize,
                        areaWidth - 12f
                );

        float textWidth =
                calculateTextWidth(
                        font,
                        safeText,
                        fittedFontSize
                );

        float xPosition =
                areaX
                        + (areaWidth - textWidth) / 2f;

        setFillColor(contentStream, color);

        contentStream.beginText();

        contentStream.setFont(
                font,
                fittedFontSize
        );

        contentStream.newLineAtOffset(
                xPosition,
                yPosition
        );

        contentStream.showText(safeText);

        contentStream.endText();
    }

    private float calculateDecorativeLineWidth(
            PDFont font,
            String text,
            float fontSize
    ) throws IOException {

        float textWidth =
                calculateTextWidth(
                        font,
                        text,
                        fontSize
                );

        return Math.min(
                Math.max(
                        textWidth + 45f,
                        170f
                ),
                380f
        );
    }

    private float calculateFittedFontSize(
            PDFont font,
            String text,
            float preferredFontSize,
            float maximumWidth
    ) throws IOException {

        float fontSize =
                preferredFontSize;

        while (fontSize > 8f
                && calculateTextWidth(
                font,
                text,
                fontSize
        ) > maximumWidth) {
            fontSize -= 0.5f;
        }

        return fontSize;
    }

    private float calculateTextWidth(
            PDFont font,
            String text,
            float fontSize
    ) throws IOException {

        return font.getStringWidth(text)
                / 1000f
                * fontSize;
    }

    private void setFillColor(
            PDPageContentStream contentStream,
            float[] color
    ) throws IOException {

        contentStream.setNonStrokingColor(
                color[0],
                color[1],
                color[2]
        );
    }

    private void setStrokeColor(
            PDPageContentStream contentStream,
            float[] color
    ) throws IOException {

        contentStream.setStrokingColor(
                color[0],
                color[1],
                color[2]
        );
    }

    /*
     * =====================================================
     * Database and Mapping Helpers
     * =====================================================
     */

    private CourseCompletion getCourseCompletionOrThrow(
            UUID completionId
    ) {
        return courseCompletionRepository
                .findById(completionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course completion not found "
                                        + "with ID: "
                                        + completionId
                        )
                );
    }

    private Certificate getCertificateOrThrow(
            UUID certificateId
    ) {
        return certificateRepository
                .findById(certificateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Certificate not found with ID: "
                                        + certificateId
                        )
                );
    }

    private void validateCertificateEligibility(
            CourseCompletion completion
    ) {
        Enrollment enrollment =
                completion.getEnrollment();

        if (enrollment.getStatus()
                != EnrollmentStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Enrollment must be completed before "
                            + "a certificate can be issued"
            );
        }

        if (!Boolean.TRUE.equals(
                completion.getCertificateEligible()
        )) {
            throw new IllegalArgumentException(
                    "Learner is not eligible for a certificate"
            );
        }

        if (!Boolean.TRUE.equals(
                enrollment.getCourse()
                        .getCertificateEnabled()
        )) {
            throw new IllegalArgumentException(
                    "Certificate generation is disabled "
                            + "for this course"
            );
        }
    }

    private String generateUniqueCertificateNumber() {
        int currentYear =
                Year.now().getValue();

        for (int attempt = 0; attempt < 20; attempt++) {
            String randomPart =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8)
                            .toUpperCase(Locale.ROOT);

            String certificateNumber =
                    "SKN-"
                            + currentYear
                            + "-"
                            + randomPart;

            boolean exists =
                    certificateRepository
                            .existsByCertificateNumberIgnoreCase(
                                    certificateNumber
                            );

            if (!exists) {
                return certificateNumber;
            }
        }

        throw new IllegalStateException(
                "Unable to generate a unique "
                        + "certificate number"
        );
    }

    private UUID generateUniqueVerificationCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            UUID verificationCode =
                    UUID.randomUUID();

            boolean exists =
                    certificateRepository
                            .findByVerificationCode(
                                    verificationCode
                            )
                            .isPresent();

            if (!exists) {
                return verificationCode;
            }
        }

        throw new IllegalStateException(
                "Unable to generate a unique "
                        + "certificate verification code"
        );
    }

    private String sanitizePdfText(
            String text
    ) {
        if (text == null || text.isBlank()) {
            return "";
        }

        StringBuilder sanitizedText =
                new StringBuilder();

        for (char character
                : text.trim().toCharArray()) {

            if (character >= 32
                    && character <= 126) {
                sanitizedText.append(character);
            } else {
                sanitizedText.append('?');
            }
        }

        return sanitizedText.toString();
    }

    private String formatDate(
            LocalDateTime dateTime
    ) {
        if (dateTime == null) {
            return "N/A";
        }

        return dateTime.format(
                DATE_FORMATTER
        );
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

    private CertificateResponseDTO mapToResponse(
            Certificate certificate
    ) {
        CourseCompletion completion =
                certificate.getCourseCompletion();

        Enrollment enrollment =
                completion.getEnrollment();

        boolean valid =
                certificate.getStatus()
                        == CertificateStatus.ISSUED;

        return CertificateResponseDTO.builder()
                .certificateId(
                        certificate.getCertificateId()
                )
                .completionId(
                        completion.getCompletionId()
                )
                .enrollmentId(
                        enrollment.getEnrollmentId()
                )
                .learnerId(
                        certificate.getLearnerId()
                )
                .courseId(
                        certificate.getCourseId()
                )
                .courseCode(
                        certificate.getCourseCode()
                )
                .courseTitle(
                        certificate.getCourseTitle()
                )
                .recipientName(
                        certificate.getRecipientName()
                )
                .certificateNumber(
                        certificate.getCertificateNumber()
                )
                .verificationCode(
                        certificate.getVerificationCode()
                )
                .status(
                        certificate.getStatus()
                )
                .valid(valid)
                .issuedByUserId(
                        certificate.getIssuedByUserId()
                )
                .revokedByUserId(
                        certificate.getRevokedByUserId()
                )
                .fileUrl(
                        certificate.getFileUrl()
                )
                .courseCompletedAt(
                        certificate.getCourseCompletedAt()
                )
                .issuedAt(
                        certificate.getIssuedAt()
                )
                .revokedAt(
                        certificate.getRevokedAt()
                )
                .revocationReason(
                        certificate.getRevocationReason()
                )
                .createdAt(
                        certificate.getCreatedAt()
                )
                .updatedAt(
                        certificate.getUpdatedAt()
                )
                .build();
    }
}