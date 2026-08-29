package com.skillspherenexus.skillmanagementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillspherenexus.skillmanagementservice.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

}