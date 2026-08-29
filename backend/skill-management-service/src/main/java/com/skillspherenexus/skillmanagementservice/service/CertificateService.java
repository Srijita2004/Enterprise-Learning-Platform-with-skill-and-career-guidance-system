package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;
import java.util.Optional;

import com.skillspherenexus.skillmanagementservice.dto.CertificateRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.CertificateResponseDTO;

public interface CertificateService {

    CertificateResponseDTO saveCertificate(CertificateRequestDTO request);

    List<CertificateResponseDTO> getAllCertificates();

    List<CertificateResponseDTO> getCertificatesByEmployee(Integer empid);

    Optional<CertificateResponseDTO> getCertificateById(Integer id);

    CertificateResponseDTO updateCertificate(Integer id, CertificateRequestDTO request);

    void deleteCertificate(Integer id);
}