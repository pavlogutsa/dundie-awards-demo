package com.ninjaone.dundie_awards.repository;

import com.ninjaone.dundie_awards.model.Employee;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByOrganizationId(Long organizationId);
    
    Page<Employee> findByOrganizationId(Long organizationId, Pageable pageable);

}
