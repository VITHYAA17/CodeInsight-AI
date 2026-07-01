package com.codeinsight.backend.repository;

import com.codeinsight.backend.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    Optional<Statistics> findByUserIdAndPlatform(Long userId, String platform);
    List<Statistics> findByUserId(Long userId);
}
