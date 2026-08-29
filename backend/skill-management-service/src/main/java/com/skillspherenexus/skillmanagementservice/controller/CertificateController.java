package com.skillspherenexus.skillmanagementservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.skillmanagementservice.dto.CertificateRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.CertificateResponseDTO;
import com.skillspherenexus.skillmanagementservice.service.CertificateService;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    @PostMapping
    public ResponseEntity<CertificateResponseDTO> addCertificate(@RequestBody CertificateRequestDTO request) {
        return ResponseEntity.ok(certificateService.saveCertificate(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping
    public ResponseEntity<List<CertificateResponseDTO>> getAllCertificates() {
        return ResponseEntity.ok(certificateService.getAllCertificates());
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping("/employee/{empid}")
    public ResponseEntity<List<CertificateResponseDTO>> getCertificatesByEmployee(@PathVariable Integer empid) {
        return ResponseEntity.ok(certificateService.getCertificatesByEmployee(empid));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponseDTO> getCertificateById(@PathVariable Integer id) {
        return certificateService.getCertificateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}")
    public ResponseEntity<CertificateResponseDTO> updateCertificate(@PathVariable Integer id,
                                                                    @RequestBody CertificateRequestDTO request) {
        try {
            return ResponseEntity.ok(certificateService.updateCertificate(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCertificate(@PathVariable Integer id) {
        certificateService.deleteCertificate(id);
        return ResponseEntity.ok("Certificate deleted successfully");
    }
}