package com.ninjaone.dundie_awards.repository;

import com.ninjaone.dundie_awards.model.Award;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AwardRepository extends JpaRepository<Award, Long> {

    List<Award> findByEmployeeId(Long employeeId);
}
