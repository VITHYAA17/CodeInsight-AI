package com.codeinsight.backend.repository;

import com.codeinsight.backend.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    List<StudyPlan> findByUserId(Long userId);
    List<StudyPlan> findByUserIdAndStatus(Long userId, String status);
    List<StudyPlan> findByUserIdOrderByWeekNumberAsc(Long userId);
    Optional<StudyPlan> findByUserIdAndWeekNumber(Long userId, Integer weekNumber);
    void deleteByUserId(Long userId);
}
