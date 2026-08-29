package com.skillspherenexus.skillmanagementservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer certid;

    @Column(name = "empid", nullable = false)
    private Integer empid; // Links certificate to a specific Employee

    @Column(name = "name", nullable = false)
    private String name; // Certificate name [cite: 58, 82]

    private String issuingOrganization;

    private LocalDate issueDate;

    @Column(name = "expiry")
    private LocalDate expiry; // Expiry date for validation [cite: 59, 82]

    private String status; // Valid / Expired status tracking [cite: 111]
}
