package com.codeinsight.backend.repository;

import com.codeinsight.backend.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUserId(Long userId);
    Optional<Recommendation> findByUserIdAndTargetCompany(Long userId, String targetCompany);
    List<Recommendation> findByUserIdOrderByGeneratedAtDesc(Long userId);
}
