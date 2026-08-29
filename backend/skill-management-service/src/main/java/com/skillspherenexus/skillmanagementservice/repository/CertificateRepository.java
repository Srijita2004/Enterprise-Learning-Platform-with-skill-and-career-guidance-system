package com.skillspherenexus.skillmanagementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillspherenexus.skillmanagementservice.entity.Certificate;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Integer> {
    // Milestone 1: Employee skill profile lookup filter
    List<Certificate> findByEmpid(Integer empid);
}
