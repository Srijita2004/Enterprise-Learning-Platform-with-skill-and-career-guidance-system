package com.skillspherenexus.skillmanagementservice.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skillspherenexus.skillmanagementservice.dto.CertificateRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.CertificateResponseDTO;
import com.skillspherenexus.skillmanagementservice.entity.Certificate;
import com.skillspherenexus.skillmanagementservice.repository.CertificateRepository;

@Service
public class CertificateServiceImpl implements CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateServiceImpl.class);

    @Autowired
    private CertificateRepository certificateRepository;

    @Override
    public CertificateResponseDTO saveCertificate(CertificateRequestDTO request) {
        Certificate certificate = new Certificate();
        certificate.setEmpid(request.getEmpid());
        certificate.setName(request.getName());
        certificate.setIssuingOrganization(request.getIssuingOrganization());
        certificate.setIssueDate(request.getIssueDate());
        certificate.setExpiry(request.getExpiry());
        certificate.setStatus(request.getStatus());

        validateExpiryStatus(certificate);
        return convertToResponse(certificateRepository.save(certificate));
    }

    @Override
    public List<CertificateResponseDTO> getAllCertificates() {
        List<Certificate> list = certificateRepository.findAll();
        list.forEach(this::validateExpiryStatus);
        return list.stream().map(this::convertToResponse).toList();
    }

    @Override
    public List<CertificateResponseDTO> getCertificatesByEmployee(Integer empid) {
        List<Certificate> certificates = certificateRepository.findByEmpid(empid);
        certificates.forEach(this::validateExpiryStatus);
        return certificates.stream().map(this::convertToResponse).toList();
    }

    @Override
    public Optional<CertificateResponseDTO> getCertificateById(Integer id) {
        Optional<Certificate> cert = certificateRepository.findById(id);
        cert.ifPresent(this::validateExpiryStatus);
        return cert.map(this::convertToResponse);
    }

    @Override
    public void deleteCertificate(Integer id) {
        certificateRepository.deleteById(id);
    }

    @Override
    public CertificateResponseDTO updateCertificate(Integer id, CertificateRequestDTO request) {

        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Certificate not found: id={}", id);
                    return new RuntimeException("Certificate not found");
                });

        certificate.setEmpid(request.getEmpid());
        certificate.setName(request.getName());
        certificate.setIssuingOrganization(request.getIssuingOrganization());
        certificate.setIssueDate(request.getIssueDate());
        certificate.setExpiry(request.getExpiry());
        certificate.setStatus(request.getStatus());

        validateExpiryStatus(certificate);

        return convertToResponse(certificateRepository.save(certificate));
    }

    /**
     * Updates certificate status based on expiry date.
     */
    private void validateExpiryStatus(Certificate certificate) {

        if (certificate.getExpiry() != null) {

            if (certificate.getExpiry().isBefore(LocalDate.now())) {
                certificate.setStatus("Expired");
            } else {
                certificate.setStatus("Valid");
            }

        }
    }

    private CertificateResponseDTO convertToResponse(Certificate certificate) {
        CertificateResponseDTO response = new CertificateResponseDTO();
        response.setCertid(certificate.getCertid());
        response.setEmpid(certificate.getEmpid());
        response.setName(certificate.getName());
        response.setIssuingOrganization(certificate.getIssuingOrganization());
        response.setIssueDate(certificate.getIssueDate());
        response.setExpiry(certificate.getExpiry());
        response.setStatus(certificate.getStatus());
        return response;
    }
}